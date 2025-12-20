package com.is.inspirationspaceclient.chat.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SessionType {
    GROUP(1, "群聊"),
    PRIVATE(2, "私聊");

    @EnumValue
    private final int code;
    private final String desc;
    SessionType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SessionType getByCode(int code) {
        for (SessionType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return PRIVATE;
    }
}
