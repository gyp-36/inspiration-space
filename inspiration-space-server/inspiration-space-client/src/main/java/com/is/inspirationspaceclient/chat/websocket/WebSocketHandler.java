package com.is.inspirationspaceclient.chat.websocket;

import com.alibaba.fastjson2.JSON;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.is.inspirationspaceclient.chat.mapper.ChatMessageMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMemberMapper;
import com.is.inspirationspaceclient.chat.model.entity.ChatMessage;
import com.is.inspirationspaceclient.chat.model.entity.ChatSession;
import com.is.inspirationspaceclient.chat.model.entity.ChatSessionMember;
import com.is.inspirationspaceclient.chat.model.entity.enums.MsgStatus;
import com.is.inspirationspaceclient.chat.model.entity.enums.SessionStatus;
import com.is.inspirationspaceclient.chat.model.entity.enums.SessionType;
import com.is.inspirationspaceclient.chat.service.ChatMessageService;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsServiceException;
import com.is.inspirationspacecommon.redis.RedisCache;
import com.is.inspirationspacecommon.redis.RedisKeyBuild;
import com.is.inspirationspacecommon.redis.RedisKeyManage;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class WebSocketHandler extends AbstractWebSocketHandler {

    // 存储活跃连接（userId -> WebSocketSession）

    private final RabbitTemplate rabbitTemplate;
    private final RedisCache redisCache;
    private final ConnectionManager connectionManager;
    private final ChatMessageMapper chatMessageMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatSessionMemberMapper chatSessionMemberMapper;


    /**
     * 连接建立处理
     */
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        //从前端获取token
        String token = extractTokenFromSession(session);
        if (token == null || !JwtUtil.validateToken(token)) {
            session.sendMessage(new TextMessage("无效token"));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        Long userId = JwtUtil.getUserIdFromToken(token);
        String username = JwtUtil.getUsernameFromToken(token);

        if (userId == null) {
            session.sendMessage(new TextMessage("userId为空"));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        // 存储会话属性
        session.getAttributes().put("userId", userId);
        session.getAttributes().put("username", username);

        // 注册连接
        connectionManager.register(userId, session);

        // 设置Redis在线状态（10分钟）
        RedisKeyBuild onlineKey = RedisKeyBuild.createRedisKey(RedisKeyManage.USER_ONLINE, userId);
        redisCache.set(onlineKey, userId, 10, TimeUnit.MINUTES);


        //拉取离线消息
        log.info("拉取离线消息，userId: {}", userId);
    }


    /**
     * 接收消息处理
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long senderId = (Long) session.getAttributes().get("userId");
        String payload = message.getPayload();
        ChatMessage chatMessage = parseMessage(payload);

        // 判断会话成员是否有效
        Integer validCount = chatSessionMemberMapper.selectValidSessionMemberCount(
                chatMessage.getSessionId(),
                senderId,
                SessionStatus.NORMAL
        );
        if (validCount <= 0) {
            throw new IsServiceException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "无效会话");
        }

        // 查询会话信息
        ChatSession chatSession = chatSessionMapper.selectOne(
                new QueryWrapper<ChatSession>()
                        .select("session_type")
                        .eq("session_id", chatMessage.getSessionId())
        );
        if (chatSession == null) {
            log.error("会话不存在，sessionId: {}", chatMessage.getSessionId());
            return;
        }

        // 处理消息
        if (payload.contains("readAck")) {
            markMessageAsRead(chatMessage.getMessageId(), senderId);
        } else {
            // 根据消息类型分别处理
            if (chatMessage.getStatus() == MsgStatus.RECALLED) {
                // 处理消息撤回
                processMessageRecall(chatSession, chatMessage, senderId);
            } else {
                // 处理普通消息发送
                processNormalMessage(chatSession, chatMessage, senderId);
            }
        }

    }

    /**
     * 标记消息为已读状态
     */
    private void markMessageAsRead(Long messageId, Long userId) {
        ChatMessage message = chatMessageMapper.selectOne(
                new QueryWrapper<ChatMessage>()
                        .select("session_status")
                        .eq("message_id", messageId)
        );
        if (message == null) return;

        // 仅允许接收方标记为已读
        if (!message.getSenderId().equals(userId)) {
            message.setStatus(MsgStatus.READ);
            chatMessageMapper.updateById(message);
        }
    }

    /**
     * 处理消息撤回
     */
    private void processMessageRecall(ChatSession chatSession, ChatMessage recallRequest, Long senderId) {
        // 1. 验证要撤回的消息是否存在且未被撤回
        ChatMessage targetMessage = chatMessageMapper.selectOne(
                new QueryWrapper<ChatMessage>()
                        .eq("message_id", recallRequest.getMessageId())
                        .eq("session_id", recallRequest.getSessionId())
                        .ne("status", MsgStatus.RECALLED)
        );

        if (targetMessage == null) {
            log.warn("尝试撤回不存在或已撤回的消息，messageId: {}", recallRequest.getMessageId());
            return;
        }

        // 2. 验证撤回权限（只能撤回自己发送的消息）
        if (!targetMessage.getSenderId().equals(senderId)) {
            log.warn("用户 {} 尝试撤回他人消息 {}", senderId, recallRequest.getMessageId());
            return;
        }

        // 3. 验证撤回时间限制（2分钟内可撤回）
        LocalDateTime sentTime = targetMessage.getSentAt();
        if (sentTime == null || LocalDateTime.now().isAfter(sentTime.plusMinutes(2))) {
            log.warn("消息撤回超时，messageId: {}", recallRequest.getMessageId());
            return;
        }

        // 4. 更新原消息状态为已撤回
        targetMessage.setStatus(MsgStatus.RECALLED);
        targetMessage.setUpdateAt(LocalDateTime.now());
        chatMessageMapper.updateById(targetMessage);

        // 5. 通知会话中的其他成员该消息已被撤回
        if (chatSession.getSessionType() == SessionType.GROUP) {
            handleGroupMessage(chatSession, targetMessage);  // 发送更新后的消息状态
        } else if (chatSession.getSessionType() == SessionType.PRIVATE) {
            handlePrivateMessage(chatSession, targetMessage);
        }
    }

    /**
     * 处理普通消息发送
     */
    private void processNormalMessage(ChatSession chatSession, ChatMessage chatMessage, Long senderId) {
        // 设置基础消息字段
        chatMessage.setMessageId(snowflakeIdGenerator.nextId());
        chatMessage.setSenderId(senderId);
        chatMessage.setSentAt(LocalDateTime.now());
        chatMessage.setStatus(MsgStatus.SENT);

        // 保存消息到数据库
        chatMessageMapper.insert(chatMessage);

        // 根据会话类型分发消息
        if (chatSession.getSessionType() == SessionType.GROUP) {
            handleGroupMessage(chatSession, chatMessage);
        } else if (chatSession.getSessionType() == SessionType.PRIVATE) {
            handlePrivateMessage(chatSession, chatMessage);
        }
    }


    /**
     * 处理二进制消息
     */
    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage binaryMessage) {
        // 获取发送者ID
        Long senderId = (Long) session.getAttributes().get("userId");

        // 记录二进制消息接收日志
        log.info("收到用户 {} 的二进制消息，大小: {} bytes", senderId, binaryMessage.getPayloadLength());

        try {
            // 获取二进制数据
            byte[] payload = binaryMessage.getPayload().array();
            // 1. 文件处理
            //    - 解析文件头部信息识别文件类型
            //    - 验证文件大小和格式
            //    - 上传到文件存储系统
            //    - 生成文件访问URL
            //    - 创建ChatMessage记录文件信息
            //    - 通知接收方有文件消息

            // 2. 图片处理
            //    - 验证图片格式(JPG/PNG/GIF等)
            //    - 生成缩略图
            //    - 上传到CDN
            //    - 获取图片访问URL
            //    - 创建ChatMessage记录图片信息

            // 3. 后续扩展支持
            //    - 视频/音频文件处理
            //    - 压缩包解压处理
            //    - 文档格式转换处理

            // 示例: 回复确认消息
            session.sendMessage(new TextMessage("{\"type\":\"binary_ack\",\"status\":\"received\"}"));

        } catch (Exception e) {
            log.error("处理二进制消息失败: {}", e.getMessage());
            try {
                session.sendMessage(new TextMessage("{\"type\":\"binary_error\",\"status\":\"failed\"}"));
            } catch (IOException ioException) {
                log.error("发送二进制消息错误响应失败: {}", ioException.getMessage());
            }
        }
    }


    /**
     * 处理私聊消息
     */
    private void handlePrivateMessage(ChatSession chatSession, ChatMessage chatMessage) {
        // 1. 获取接收方用户ID
        Long receiverId = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .select("user_id")
                        .eq("session_id", chatSession.getSessionId())
                        .ne("user_id", chatMessage.getSenderId())
        ).getUserId();
        // 2. 查找接收方是否在线(在线则直接推送，不在线则存储为离线消息)
        if (connectionManager.isOnline(receiverId)) {
            //直接推送
            connectionManager.sendIfOnline(receiverId, session -> {
                try {
                    session.sendMessage(new TextMessage(JSON.toJSONString(chatMessage)));
                    chatMessage.setStatus(MsgStatus.DELIVERED);//更新为已送达
                    chatMessageMapper.updateById(chatMessage);
                } catch (IOException e) {
                    log.error("发送消息失败：{}", e.getMessage());
                }
            });
        } else {
            //存储离线消息
            rabbitTemplate.convertAndSend("notification_exchange", "offline." + receiverId, chatMessage);
        }
    }


    /**
     * 处理群聊消息
     */
    private void handleGroupMessage(ChatSession chatSession, ChatMessage chatMessage) {
        // 1. 获取群聊所有成员列表
        List<ChatSessionMember> members = chatSessionMemberMapper.selectList(
                new QueryWrapper<ChatSessionMember>()
                        .select("user_id")
                        .eq("session_id", chatSession.getSessionId())
                        .eq("session_status", SessionStatus.NORMAL)
                        .ne("user_id", chatMessage.getSenderId())
        );


        // 2. 向每个成员推送消息
        for (ChatSessionMember member : members) {
            Long memberId = member.getUserId();
            if (connectionManager.isOnline(memberId)) {
                // 直接推送
                connectionManager.sendIfOnline(memberId, session -> {
                    try {
                        chatMessage.setStatus(MsgStatus.DELIVERED);//更新为已送达
                        session.sendMessage(new TextMessage(JSON.toJSONString(chatMessage)));
                    } catch (IOException e) {
                        log.error("发送群聊消息失败：{}", e.getMessage());
                    }
                });
            } else {
                // 存储离线消息到RabbitMQ队列
                rabbitTemplate.convertAndSend("notification_exchange", "offline." + memberId, chatMessage);
            }
        }
    }


    /**
     * 连接关闭处理
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            connectionManager.unregister(userId);
            RedisKeyBuild onlineKey = RedisKeyBuild.createRedisKey(RedisKeyManage.USER_ONLINE, userId);
            redisCache.del(onlineKey);
        }
    }


    /**
     * 从session中提取token
     */
    private String extractTokenFromSession(WebSocketSession session) {
        String uri = session.getUri().toString();
        Map<String, List<String>> params = UriComponentsBuilder.fromUriString(uri).build().getQueryParams();
        List<String> tokens = params.get("token");
        return tokens != null && !tokens.isEmpty() ? tokens.get(0) : null;
    }

    /**
     * 解析消息体
     */
    private ChatMessage parseMessage(String json) {
        return JSON.parseObject(json, ChatMessage.class);
    }

    /**
     * 序列化消息
     */
    private String serialize(ChatMessage message) {
        return JSON.toJSONString(message);
    }
}
