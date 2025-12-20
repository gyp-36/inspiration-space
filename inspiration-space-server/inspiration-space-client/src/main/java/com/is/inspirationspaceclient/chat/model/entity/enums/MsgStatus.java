package com.is.inspirationspaceclient.chat.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MsgStatus {
    INNORMAL(0, "异常"),
    NORMAL(1, "正常"),
    RECALLED(2, "已撤回"),
    NOT_SENT(3, "未发送"),
    SENT(4,"已发送"),
    DELIVERED(5, "已送达"),
    READ(6, "已读");

    @EnumValue
    private Integer code;
    private String desc;

    MsgStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MsgStatus getByCode(Integer code) {
        for (MsgStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return INNORMAL;
    }

}
