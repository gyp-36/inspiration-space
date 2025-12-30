package com.is.inspirationspaceclient.chat.model.vo;

import com.is.inspirationspaceclient.chat.model.entity.enums.SessionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "会话列表项")
public class ChatSessionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(name = "sessionId", type = "Long", description = "会话ID")
    private Long sessionId;

    @Schema(name = "sessionName", type = "String", description = "会话名称")
    private String sessionName;

    @Schema(name = "sessionAvatar", type = "String", description = "会话头像")
    private String sessionAvatar;

    @Schema(name = "sessionType", type = "SessionType", description = "会话类型")
    private SessionType sessionType;

    @Schema(name = "targetUserId", type = "Long", description = "对方用户ID（私聊时有效）")
    private Long targetUserId;

    @Schema(name = "lastMessage", type = "String", description = "最后一条消息内容")
    private String lastMessage;

    @Schema(name = "lastMessageTime", type = "String", description = "最后一条消息时间")
    private String lastMessageTime;

    @Schema(name = "unreadCount", type = "Integer", description = "未读消息数")
    private Integer unreadCount;
}