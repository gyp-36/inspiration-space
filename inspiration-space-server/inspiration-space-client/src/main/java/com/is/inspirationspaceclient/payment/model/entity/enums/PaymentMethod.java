package com.is.inspirationspaceclient.payment.model.entity.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    WECHAT(1, "微信"),
    ALIPAY(2, "支付宝"),
    BANK_CARD(3, "银行卡"),
    OTHER(4, "其他");

    private final int code;
    private final String desc;

    PaymentMethod(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PaymentMethod getByCode(Integer code) {
        for (PaymentMethod value : PaymentMethod.values()) {
            if (value.code==(code)) {
                return value;
            }
        }
        return null;
    }
}