package com.is.inspirationspaceclient.payment.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum RefundStatus {
    REFUNDING(0, "退款中"),
    REFUND_SUCCESS(1, "退款成功"),
    REFUND_FAILED(2, "退款失败"),
    REFUND_CANCEL(3, "退款取消");

    @EnumValue
    private final int code;
    private final String desc;

    RefundStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RefundStatus getDescByCode(Integer code) {
        for (RefundStatus value : RefundStatus.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return REFUNDING;
    }
}
