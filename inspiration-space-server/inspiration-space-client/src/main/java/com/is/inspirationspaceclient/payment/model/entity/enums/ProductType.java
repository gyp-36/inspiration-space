package com.is.inspirationspaceclient.payment.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ProductType {
    MEMBER(1, "会员"),
    PRODUCT(2, "商品");

    @EnumValue
    private final int code;
    private final String desc;

    ProductType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProductType getByCode(Integer code) {
        for (ProductType value : values()) {
            if (value.code==(code)) {
                return value;
            }
        }
        return null;
    }
}