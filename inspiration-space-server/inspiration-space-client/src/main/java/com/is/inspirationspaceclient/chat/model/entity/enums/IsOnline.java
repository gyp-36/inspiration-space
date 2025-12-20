package com.is.inspirationspaceclient.chat.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum IsOnline {

    ONLINE(1, "在线"),
    OFFLINE(0, "离线");

    @EnumValue
    private final int code;
    private final String desc;

    IsOnline(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
