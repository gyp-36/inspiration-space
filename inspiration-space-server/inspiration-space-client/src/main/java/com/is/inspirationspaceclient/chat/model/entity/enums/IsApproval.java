package com.is.inspirationspaceclient.chat.model.entity.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum IsApproval {
    NEED_APPROVAL(0, "需要审核"),
    NEED_NOT_APPROVAL(1, "不需要审核");

    @EnumValue
    private final int code;
    private final String desc;

    IsApproval(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static IsApproval getDesc(int code) {
        for (IsApproval value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return NEED_NOT_APPROVAL;
    }
}
