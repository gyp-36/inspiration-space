package com.is.inspirationspaceclient.notification.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import org.springframework.security.core.parameters.P;

/**
 * 模板状态枚举
 */
@Getter
public enum TemplateStatus {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    @EnumValue
    private final int code;
    private final String desc;

    TemplateStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TemplateStatus fromCode(int code) {
        for (TemplateStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return ENABLED;
    }


}