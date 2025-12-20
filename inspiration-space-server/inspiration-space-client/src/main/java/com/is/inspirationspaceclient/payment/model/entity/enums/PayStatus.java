package com.is.inspirationspaceclient.payment.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PayStatus {
    UNPAID(0, "支付中"),
    SUCCESS(1, "支付成功"),
    FAIL(2, "支付失败"),
    CANCEL(3, "支付取消");

    @EnumValue
    private final int code;
    private final String desc;

    PayStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayStatus getDescByCode(Integer code) {
        for (PayStatus value : PayStatus.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return UNPAID;
    }

}
