package com.is.inspirationspaceclient.user.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 关注类型枚举
 */
@Getter
public enum FollowType {
    UNFOLLOW(0, "取消关注"),
    FOLLOW(1, "关注"),
    SPECIAL_FOLLOW(2, "特别关注"),
    BLACKED(3, "拉黑");

    @EnumValue
    private final int code;
    private  final String description;

    FollowType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static FollowType getByCode(int code) {
        for (FollowType followType : FollowType.values()) {
            if (followType.code == code) {
                return followType;
            }
        }
        return FOLLOW;
    }
}
