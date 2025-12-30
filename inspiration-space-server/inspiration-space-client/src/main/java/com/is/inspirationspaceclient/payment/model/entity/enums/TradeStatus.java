package com.is.inspirationspaceclient.payment.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum TradeStatus {
    CREATED(0, "创建"),
    UNPAY(1, "未支付"),
    PAYING(2, "支付中"),
    PAY(3, "已支付"),
    REFUND(4, "退款"),
    DELETED(5, "已删除");

    @EnumValue
    private final int code;
    private final String desc;

    TradeStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TradeStatus getDescByCode(Integer code) {
        for (TradeStatus value : TradeStatus.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return CREATED;
    }
}