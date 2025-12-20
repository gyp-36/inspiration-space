package com.is.inspirationspaceclient.user.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 权限状态
 */

@Getter
public enum PermissionStatus {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    @EnumValue
    private final Integer code;
    private final String message;

    PermissionStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static PermissionStatus getByCode(Integer code) {
        for (PermissionStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return ENABLE;
    }

}
