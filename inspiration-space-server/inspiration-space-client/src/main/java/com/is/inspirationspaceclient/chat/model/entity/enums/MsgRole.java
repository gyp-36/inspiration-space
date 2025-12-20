package com.is.inspirationspaceclient.chat.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import org.simpleframework.xml.Element;

@Getter
public enum MsgRole {

    NORMAL(1,"普通成员"),
    ADMIN(2,"管理员"),
    OWNER(3,"群主");

    @EnumValue
    private Integer code;
    private String desc;

    MsgRole(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MsgRole getByCode(Integer code) {
        for (MsgRole value : MsgRole.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return NORMAL;
    }
}
