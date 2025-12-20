package com.is.inspirationspaceclient.chat.model.dto;

import com.is.inspirationspaceclient.chat.model.entity.enums.MsgType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发送消息请求参数")
public class SendMessageRequestDto {

    @Schema(name = "sessionId",type = "Long",description = "会话ID")
    private Long sessionId;

    @Schema(name = "msgType",type = "MsgType",description = "消息类型")
    private MsgType msgType;

    @Schema(name = "content",type = "String",description = "消息内容")
    private String content;

    @Schema(name = "fileUrl",type = "String",description = "文件URL")
    private String fileUrl;



}