package com.is.inspirationspaceclient.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.is.inspirationspaceclient.chat.mapper.ChatMessageMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMemberMapper;
import com.is.inspirationspaceclient.chat.model.entity.ChatMessage;
import com.is.inspirationspaceclient.chat.model.entity.ChatSessionMember;
import com.is.inspirationspaceclient.chat.model.entity.enums.MsgStatus;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private ChatMessageMapper chatMessageMapper;
    private ChatSessionMemberMapper chatSessionMemberMapper;

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

        // 统计这些会话中未读的消息数量（状态为DELIVERED且发送者不是当前用户）
        Long count = chatMessageMapper.selectCount(
                new QueryWrapper<ChatMessage>()
                        .in("session_id", userSessions)
                        .ne("sender_id", currentUserId)  // 不是当前用户发送的消息
                        .eq("status", MsgStatus.DELIVERED)  // 消息状态为已送达但未读
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
        
        // 查询会话中的消息历史，按时间倒序排列，限制返回最近100条
        return chatMessageMapper.selectList(
                new QueryWrapper<ChatMessage>()
                        .eq("session_id", sessionId)
                        .orderByDesc("sent_at")
                        .last("LIMIT 100")
        );
    }
}