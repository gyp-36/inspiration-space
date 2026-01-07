package com.is.inspirationspaceclient.chat.rabbitmq;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.is.inspirationspaceclient.chat.mapper.ChatMessageMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMemberMapper;
import com.is.inspirationspaceclient.chat.model.entity.ChatMessage;
import com.is.inspirationspaceclient.chat.model.entity.ChatSessionMember;
import com.is.inspirationspaceclient.chat.model.entity.enums.MsgStatus;
import com.is.inspirationspaceclient.chat.websocket.ConnectionManager;
import com.is.inspirationspacecommon.redis.RedisCache;
import com.is.inspirationspacecommon.redis.RedisKeyBuild;
import com.is.inspirationspacecommon.redis.RedisKeyManage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class OfflineMessageListener {

    private final ConnectionManager connectionManager;
    private final ChatMessageMapper chatMessageMapper;
    private final RedisCache redisCache;
    private final ChatSessionMemberMapper chatSessionMemberMapper;


    /**
     * 监听离线消息队列
     * 根据RabbitMQ配置，监听 offline.* 路由键模式的消息
     */
    @RabbitListener(queues = "chat.offline_queue")
    public void handleOfflineMessage(ChatMessage chatMessage) {
        try {
            // 从消息中提取接收者ID
            Long receiverId = getReceiverIdFromSession(chatMessage);
            if (receiverId == null) {
                log.warn("无法确定消息 {} 的接收者", chatMessage.getMessageId());
                return;
            }

            // 将消息写入Redis Stream，按receiverId分组
            RedisKeyBuild streamKey = RedisKeyBuild.createRedisKey(
                    RedisKeyManage.OFFLINE_MESSAGE_STREAM, receiverId.toString());

            Map<String, Object> messageBody = new HashMap<>();
            messageBody.put("chatMessage", chatMessage);
            messageBody.put("receiverId", receiverId);
            messageBody.put("createTime", LocalDateTime.now());

            // 写入Redis Stream
            redisCache.rightPushList(streamKey, messageBody);

            log.debug("消息已写入Redis: streamKey={}", streamKey.getRelKey());

            // 立即尝试处理该receiver的消息流
            processUserMessages(receiverId);

        } catch (Exception e) {
            log.error("处理离线消息写入Redis异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理指定用户的消息队列
     */
    private void processUserMessages(Long receiverId) {
        RedisKeyBuild streamKey = RedisKeyBuild.createRedisKey(
                RedisKeyManage.OFFLINE_MESSAGE_STREAM, receiverId.toString());

        try {
            // 使用分布式锁确保同一时间只有一个消费者处理该用户的消息
            String lockKey = "lock:offline_msg:" + receiverId;
            if (redisCache.tryLock(lockKey, 3, 30, TimeUnit.SECONDS)) {
                try {
                    // 循环处理队列中的消息直到为空
                    while (true) {
                        Map<String, Object> messageData = redisCache.leftPopList(streamKey, Map.class);
                        if (messageData == null) {
                            break; // 队列为空，退出循环
                        }

                        try {
                            ChatMessage chatMessage = (ChatMessage) messageData.get("chatMessage");
                            Long msgReceiverId = (Long) messageData.get("receiverId");

                            // 处理单条消息
                            boolean processed = processSingleMessage(chatMessage, msgReceiverId);

                            // 如果处理失败，重新放回队列尾部
                            if (!processed) {
                                redisCache.rightPushList(streamKey, messageData);
                                //更新最后一条只读消息位置
                                chatSessionMemberMapper.update(
                                        new ChatSessionMember(), // 需要更新的实体对象
                                        new UpdateWrapper<ChatSessionMember>()
                                                .eq("session_id", chatMessage.getSessionId())
                                                .eq("user_id", receiverId)
                                                .set("last_read_message_id", chatMessage.getMessageId())
                                );
                                break; // 暂停处理，等待用户上线
                            }

                        } catch (Exception e) {
                            log.error("处理消息异常，重新放回队列: {}", e.getMessage(), e);
                            redisCache.rightPushList(streamKey, messageData);
                        }
                    }
                } finally {
                    redisCache.unlock(lockKey);
                }
            }
        } catch (Exception e) {
            log.error("处理用户 {} 的消息队列异常: {}", receiverId, e.getMessage(), e);
        }
    }


    /**
     * 处理单条消息
     */
    private boolean processSingleMessage(ChatMessage chatMessage, Long receiverId) {
        try {
            // 检查接收用户是否在线
            if (connectionManager.isOnline(receiverId)) {
                // 用户在线，直接推送消息
                final boolean[] success = {false};
                connectionManager.sendIfOnline(receiverId, session -> {
                    try {
                        if (chatMessage.getStatus() == MsgStatus.SENT) {
                            String messageJson = JSON.toJSONString(chatMessage);
                            session.sendMessage(new TextMessage(messageJson));

                            // 更新消息状态为"已送达"
                            chatMessage.setStatus(MsgStatus.DELIVERED);
                            chatMessageMapper.updateById(chatMessage);

                            log.info("成功推送离线消息 messageId={}", chatMessage.getMessageId());
                            success[0] = true;
                        }
                    } catch (IOException e) {
                        log.error("推送离线消息失败: {}", e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                });
                return success[0];
            } else {
                // 用户仍不在线，保持消息在队列中等待下次处理
                log.debug("用户 {} 仍不在线，消息 {} 保留在队列中等待下次处理",
                        receiverId, chatMessage.getMessageId());
                return false;
            }
        } catch (Exception e) {
            log.error("处理单条离线消息异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从会话中获取接收者ID
     */
    private Long getReceiverIdFromSession(ChatMessage chatMessage) {
        try {
            // 查询会话中的其他成员（排除发送者）
            ChatSessionMember member = chatSessionMemberMapper.selectOne(
                    new QueryWrapper<ChatSessionMember>()
                            .select("user_id")
                            .eq("session_id", chatMessage.getSessionId())
                            .ne("user_id", chatMessage.getSenderId())
            );

            return member != null ? member.getUserId() : null;
        } catch (Exception e) {
            log.error("查询会话成员失败: {}", e.getMessage());
            return null;
        }
    }
}
