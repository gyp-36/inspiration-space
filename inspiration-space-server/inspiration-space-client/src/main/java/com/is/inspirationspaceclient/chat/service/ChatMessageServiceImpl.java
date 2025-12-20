package com.is.inspirationspaceclient.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.is.inspirationspaceclient.chat.mapper.ChatMessageMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMemberMapper;
import com.is.inspirationspaceclient.chat.model.dto.SendMessageRequestDto;
import com.is.inspirationspaceclient.chat.model.entity.ChatMessage;
import com.is.inspirationspaceclient.chat.model.entity.ChatSessionMember;
import com.is.inspirationspaceclient.chat.model.entity.enums.MsgStatus;
import com.is.inspirationspaceclient.chat.model.vo.MessageVO;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
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

}
