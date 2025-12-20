package com.is.inspirationspaceclient.work.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Status {

    WRITING(0, "草稿"),
    REVIEWING(1, "审核中"),
    PUBLISHED(2, "已发布"),
    DELETED(3, "已下架"),
    REJECTED(4, "审核未通过");

    @EnumValue
    private final int code;
    private final String description;

    Status(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Status getByCode(int code) {
        for (Status status : Status.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return WRITING;
    }

}
