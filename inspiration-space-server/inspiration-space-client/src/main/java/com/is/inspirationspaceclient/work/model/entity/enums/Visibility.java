package com.is.inspirationspaceclient.work.model.entity.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;


@Getter
public enum Visibility {
    PUBLIC(0, "公开"),
    PRIVATE(1, "仅自己可见"),
    PROTECTED(2, "受保护的"),
    SECRET(3, "保密");

    @EnumValue
    private final int code;
    private final String description;

    Visibility(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Visibility getByCode(int code) {
        for (Visibility visibility : Visibility.values()) {
            if (visibility.code == code) {
                return visibility;
            }
        }
        return PUBLIC;
    }
}
