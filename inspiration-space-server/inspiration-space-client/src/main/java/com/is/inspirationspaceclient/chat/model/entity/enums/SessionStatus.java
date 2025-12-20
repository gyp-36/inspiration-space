package com.is.inspirationspaceclient.chat.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SessionStatus {
    NORMAL(0, "正常"),
    BANNED(1, "封禁"),
    DELETED(2, "删除"),
    DISSOLVED(3, "已解散");

    @EnumValue
    private final int code;
    private final String desc;

    SessionStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SessionStatus getByCode(int code) {
        for (SessionStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return NORMAL;
    }
}
