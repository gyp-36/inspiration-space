package com.is.inspirationspaceclient.notification.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 消息状态枚举
 */
@Getter
public enum Status {
    DRAFT(0, "草稿"),
    PENDING(1, "待发送"),
    SENT(2, "已发送"),
    CANCELED(3, "已取消");


    @EnumValue
    private final int code;
    private final String desc;

    Status(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Status getByCode(int code) {
        for (Status status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING;
    }

}