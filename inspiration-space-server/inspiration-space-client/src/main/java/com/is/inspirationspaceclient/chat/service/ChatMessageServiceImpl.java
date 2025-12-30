package com.is.inspirationspaceclient.chat.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.socket.TextMessage;
import java.io.IOException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.is.inspirationspaceclient.chat.mapper.ChatMessageMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMemberMapper;
import com.is.inspirationspaceclient.chat.model.dto.SendMessageRequestDto;
import com.is.inspirationspaceclient.chat.model.entity.ChatMessage;
import com.is.inspirationspaceclient.chat.model.entity.ChatSessionMember;
import com.is.inspirationspaceclient.chat.model.entity.enums.MsgStatus;
import com.is.inspirationspaceclient.chat.websocket.ConnectionManager;
import com.is.inspirationspacecommon.config.StorageService;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMemberMapper chatSessionMemberMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final ConnectionManager connectionManager;
    private final StorageService storageService;

    private String getPresignedUrl(String objectKey) {
        if (objectKey == null || objectKey.isEmpty() || objectKey.startsWith("http")) {
            return objectKey;
        }
        try {
            // 假设聊天文件存储在 "chat" bucket 中，头像在 "avatars" bucket
            // 如果无法确定，可以尝试从路径推断或统一使用一个 bucket
            String bucketName = objectKey.startsWith("avatar") ? "avatars" : "chat";
            return storageService.getPreSignedUrl(bucketName, objectKey, 3, java.util.concurrent.TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", objectKey, e);
            return objectKey;
        }
    }

    @Override
    public Integer getUnreadCount(String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }

        // 获取用户参与的所有会话ID
        List<Long> userSessions = chatSessionMemberMapper.selectList(
                new QueryWrapper<ChatSessionMember>()
                        .select("session_id")
                        .eq("user_id", currentUserId)
        ).stream().map(ChatSessionMember::getSessionId).collect(Collectors.toList());

        if (userSessions.isEmpty()) {
            return 0;
        }

        // 统计这些会话中未读的消息数量（状态为 SENT 或 DELIVERED 且发送者不是当前用户）
        Long count = chatMessageMapper.selectCount(
                new QueryWrapper<ChatMessage>()
                        .in("session_id", userSessions)
                        .ne("sender_id", currentUserId)  // 不是当前用户发送的消息
                        .in("status", MsgStatus.SENT, MsgStatus.DELIVERED)  // 消息状态为已发送或已送达但未读
        );
        return count.intValue();
    }
    
    @Override
    public List<ChatMessage> getMessageHistory(Long sessionId, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }
        
        // 验证用户是否是会话成员
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        
        if (member == null) {
            throw new IsArgumentException(ErrorCode.FORBIDDEN_ERROR.getHttpStatusCode(), "无权限访问该会话");
        }
        
        // 查询会话中的消息历史，按时间倒序排列获取最近的100条
        List<ChatMessage> messages = chatMessageMapper.selectList(
                new QueryWrapper<ChatMessage>()
                        .eq("session_id", sessionId)
                        .orderByDesc("sent_at")
                        .last("LIMIT 100")
        );

        // 处理消息中的文件URL和内容URL（如果是图片/文件类型）
        messages.forEach(msg -> {
            if ("IMAGE".equals(msg.getMsgType()) || "FILE".equals(msg.getMsgType())) {
                msg.setContent(getPresignedUrl(msg.getContent()));
                msg.setFileUrl(getPresignedUrl(msg.getFileUrl()));
            }
        });

        // 将消息按时间正序排列（从旧到新），以便前端展示
        Collections.sort(messages, Comparator.comparing(ChatMessage::getSentAt));

        return messages;
    }

    @Override
    public Long sendMessage(SendMessageRequestDto sendMessageRequestDto, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }

        // 验证用户是否是会话成员
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sendMessageRequestDto.getSessionId())
                        .eq("user_id", currentUserId)
        );

        if (member == null) {
            throw new IsArgumentException(ErrorCode.FORBIDDEN_ERROR.getHttpStatusCode(), "您不是该会话的成员");
        }

        // 创建消息对象
        ChatMessage message = new ChatMessage();
        message.setMessageId(snowflakeIdGenerator.nextId());
        message.setSessionId(sendMessageRequestDto.getSessionId());
        message.setSenderId(currentUserId);
        message.setContent(sendMessageRequestDto.getContent());
        message.setMsgType(sendMessageRequestDto.getMsgType());
        message.setFileUrl(sendMessageRequestDto.getFileUrl());
        message.setStatus(MsgStatus.SENT);
        message.setSentAt(LocalDateTime.now());

        // 保存消息
        chatMessageMapper.insert(message);

        // 广播消息
        broadcastMessageToSession(message.getSessionId(), message);

        return message.getMessageId();
    }

    @Override
    public Boolean markMessageAsRead(Long messageId, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }

        // 获取消息
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "消息不存在");
        }

        // 验证用户是否是会话成员
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", message.getSessionId())
                        .eq("user_id", currentUserId)
        );

        if (member == null) {
            throw new IsArgumentException(ErrorCode.FORBIDDEN_ERROR.getHttpStatusCode(), "您不是该消息所属会话的成员");
        }

        // 如果是发送者自己标记已读，或者是已经标记为已读，则直接返回
        if (message.getSenderId().equals(currentUserId) || message.getStatus() == MsgStatus.READ) {
            return true;
        }

        // 更新状态为已读
        message.setStatus(MsgStatus.READ);
        chatMessageMapper.updateById(message);

        return true;
    }

    /**
     * 广播消息给会话的所有成员
     * @param sessionId 会话ID
     * @param message 要广播的消息
     */
    public void broadcastMessageToSession(Long sessionId, ChatMessage message) {
        // 获取会话的所有成员
        List<ChatSessionMember> members = chatSessionMemberMapper.selectList(
                new QueryWrapper<ChatSessionMember>()
                        .select("user_id")
                        .eq("session_id", sessionId)
        );

        if (members == null || members.isEmpty()) {
            return;
        }

        // 处理消息中的URL（如果是图片/文件类型），用于广播给在线用户
        // 注意：这里需要处理一下，因为广播的是实时消息，其他在线用户需要能直接显示
        if ("IMAGE".equals(message.getMsgType()) || "FILE".equals(message.getMsgType())) {
            message.setContent(getPresignedUrl(message.getContent()));
            message.setFileUrl(getPresignedUrl(message.getFileUrl()));
        }

        // 将消息包装为 WebSocket 协议格式
        JSONObject wsMessage = new JSONObject();
        wsMessage.put("type", "CHAT_MESSAGE");
        wsMessage.put("sessionId", sessionId);
        wsMessage.put("data", message);
        
        String messageJson = wsMessage.toJSONString();

        // 向每个在线成员发送消息
        for (ChatSessionMember member : members) {
            Long memberId = member.getUserId();
            // 不发送给消息发送者自己（避免重复通知）
            if (!memberId.equals(message.getSenderId())) {
                connectionManager.sendIfOnline(memberId, session -> {
                    try {
                        session.sendMessage(new TextMessage(messageJson));
                        //更新状态为已送达
                        message.setStatus(MsgStatus.DELIVERED);
                        chatMessageMapper.updateById(message);
                        log.info("消息已发送给用户 {}: {}", memberId, message.getContent());
                    } catch (Exception e) {
                        message.setStatus(MsgStatus.NOT_SENT);
                        chatMessageMapper.updateById(message);
                        log.error("发送消息给用户 {} 失败: {}", memberId, e.getMessage());
                    }
                });
            }
        }
    }
}