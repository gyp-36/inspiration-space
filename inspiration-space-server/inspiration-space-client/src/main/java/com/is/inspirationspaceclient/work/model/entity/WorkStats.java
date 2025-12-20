package com.is.inspirationspaceclient.work.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作品统计表
 */
@Data
@TableName("work_stats")
public class WorkStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 作品ID
     */
    @TableId(value = "work_id")
    private Long workId;

    /**
     * 浏览量
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 点赞量
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 评论量
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 收藏量
     */
    @TableField("favorite_count")
    private Integer favoriteCount;

    /**
     * 购买量
     */
    @TableField("purchase_count")
    private Integer purchaseCount;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}