package com.is.inspirationspaceclient.payment.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PaymentMethod {
    WECHAT(1, "微信", "https://img.alicdn.com/tfs/TB1Z5_m7T2gK0jSZFkXXcIQFXa-200-200.png"), // 微信图标占位
    ALIPAY(2, "支付宝", "https://img.alicdn.com/tfs/TB19S6_m7T2gK0jSZFkXXcIQFXa-200-200.png"),
    BANK_CARD(3, "银行卡", ""),
    OTHER(4, "其他", "");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
    private final String icon;

    PaymentMethod(Integer code, String desc, String icon) {
        this.code = code;
        this.desc = desc;
        this.icon = icon;
    }

    @JsonCreator
    public static PaymentMethod getByCode(Integer code) {
        for (PaymentMethod value : PaymentMethod.values()) {
            if (value.code == (code)) {
                return value;
            }
        }
        return null;
    }
}