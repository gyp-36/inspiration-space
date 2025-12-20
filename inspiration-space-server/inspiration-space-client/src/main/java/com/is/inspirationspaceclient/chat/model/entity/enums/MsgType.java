package com.is.inspirationspaceclient.chat.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MsgType {
    TEXT(1, "文本"),
    IMAGE(2, "图片"),
    FILE(3, "文件"),
    SYSTEM(4, "系统消息"),
    GROUP_NOTICE(5, "群公告"),
    GROUP_JOIN(6, "入群"),
    GROUP_LEAVE(7, "退群"),
    GROUP_APPLY(8, "入群申请"),
    GROUP_TRANSFER(9, "群主转让"),
    GROUP_DISSOLVE(10, "群解散"),
    RECALL(11, "撤回消息");


    @EnumValue
    private final int code;
    private final String desc;

    MsgType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MsgType getByCode(int code) {
        for (MsgType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return TEXT;
    }

}