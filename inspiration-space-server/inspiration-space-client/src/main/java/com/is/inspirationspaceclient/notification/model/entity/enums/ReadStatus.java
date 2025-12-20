package com.is.inspirationspaceclient.notification.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 阅读状态枚举
 */
@Getter
public enum ReadStatus {
    UNREAD(0, "未读"),
    READ(1, "已读");

    @EnumValue
    private final int code;
    private final String desc;

    ReadStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReadStatus getByCode(int code) {
        for (ReadStatus readStatus : ReadStatus.values()) {
            if (readStatus.code == code) {
                return readStatus;
            }
        }
        return UNREAD;
    }


}