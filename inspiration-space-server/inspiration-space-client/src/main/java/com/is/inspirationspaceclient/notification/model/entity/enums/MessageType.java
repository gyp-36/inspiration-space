package com.is.inspirationspaceclient.notification.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
public enum MessageType {
    TEMPLATE(1, "模板消息"),
    CUSTOM(2, "自定义消息");

    @EnumValue
    private final int code;
    private final String desc;

    MessageType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MessageType getByCode(int code) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.code == code) {
                return messageType;
            }
        }
        return CUSTOM;
    }

}