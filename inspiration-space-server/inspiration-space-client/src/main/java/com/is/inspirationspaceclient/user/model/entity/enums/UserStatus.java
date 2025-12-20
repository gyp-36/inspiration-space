package com.is.inspirationspaceclient.user.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {
    NORMAL(0, "正常"),
    DISABLED_SPEAKING(1, "禁言"),
    BANNED(2, "封禁"),
    DELETED(3, "删除");

    @EnumValue
    private final int code;
    private  final String description;

    UserStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserStatus getByCode(int code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return NORMAL;
    }
}
