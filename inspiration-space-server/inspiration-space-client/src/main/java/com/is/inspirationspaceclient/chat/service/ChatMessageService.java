package com.is.inspirationspaceclient.chat.service;

import com.is.inspirationspaceclient.chat.model.dto.SendMessageRequestDto;
import com.is.inspirationspaceclient.chat.model.entity.ChatMessage;
import com.is.inspirationspaceclient.chat.model.vo.MessageVO;

import java.util.List;

public interface ChatMessageService {

    Integer getUnreadCount(String token);
    
    List<ChatMessage> getMessageHistory(Long sessionId, String token);
}