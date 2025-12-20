package com.is.inspirationspaceclient.work.model.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum TagType {
    BEST_SELLER(0, "畅销榜"),    // 畅销榜（需关联时间范围：周榜/月榜）
    MOST_POPULAR(1, "人气榜"),   // 人气榜（基于点击/浏览量）
    TRENDING(2, "趋势榜"),       // 热门趋势（短期爆发增长）
    EDITORS_CHOICE(3, "精选榜"), // 编辑推荐（人工运营）
    USER_FAVORITE(4, "收藏榜");

    @EnumValue
    private final int value;
    private final String description;

    TagType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static TagType getByValue(int value) {
        for (TagType tagType : TagType.values()) {
            if (tagType.value == value) {
                return tagType;
            }
        }
        return null;
    }
}
