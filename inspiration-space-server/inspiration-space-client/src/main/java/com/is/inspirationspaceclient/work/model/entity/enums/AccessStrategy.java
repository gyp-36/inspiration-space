package com.is.inspirationspaceclient.work.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AccessStrategy {
    FREE(0, "免费"),
    MEMBER_FREE(1, "会员免费"),
    PAY(2, "付费");

    @EnumValue
    private final int code;
    private final String description;

    AccessStrategy(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AccessStrategy getByCode(int code) {
        for (AccessStrategy value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return FREE;
    }
}
