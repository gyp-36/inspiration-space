package com.is.inspirationspaceclient.forum.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum TargetType {
    COMMENT(1, "评论"),
    LIKE(2, "点赞"),
    COLLECT(3, "收藏"),

    REPOST(4, "转发"),
    LIKE_COMMENT(5, "点赞评论"),
    LIKE_WORK(6, "点赞作品"),
    COLLECT_WORK(7, "收藏作品");

    @EnumValue
    private final Integer code;
    private final String description;
    TargetType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TargetType getByCode(Integer code) {
        for (TargetType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }


}
