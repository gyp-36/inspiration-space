package com.is.inspirationspaceclient.chat.model.vo;

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

}