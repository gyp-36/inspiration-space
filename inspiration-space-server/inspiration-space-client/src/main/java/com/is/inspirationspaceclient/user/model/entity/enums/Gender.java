package com.is.inspirationspaceclient.user.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
public enum Gender {
    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    @EnumValue
    private final int code;
    private  final String description;

    Gender(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Gender getByCode(int code) {
        for (Gender gender : Gender.values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}
