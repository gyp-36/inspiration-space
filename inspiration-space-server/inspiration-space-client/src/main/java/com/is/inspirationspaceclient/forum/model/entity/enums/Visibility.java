package com.is.inspirationspaceclient.forum.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Visibility {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    FRIENDS(2, "仅好友可见");

    @EnumValue
    private final int code;
    private final String description;
    Visibility(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Visibility getByCode(int code) {
        for (Visibility value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return PUBLIC;
    }
}
