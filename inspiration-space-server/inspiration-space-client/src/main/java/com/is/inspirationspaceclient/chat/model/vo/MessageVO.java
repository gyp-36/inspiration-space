package com.is.inspirationspaceclient.chat.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "消息VO")
public class MessageVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;



}