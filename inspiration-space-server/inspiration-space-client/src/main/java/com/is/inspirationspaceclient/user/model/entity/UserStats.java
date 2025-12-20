package com.is.inspirationspaceclient.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_stats")
public class UserStats implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId("user_id")
    private Long userId;

    /**
     * 点赞数
     */
    @TableField("likes_count")
    private Integer likesCount;

    /**
     * 粉丝数
     */
    @TableField("fans_count")
    private Integer fansCount;

    /**
     * 关注数
     */
    @TableField("followings_count")
    private Integer followingsCount;

    /**
     * 收藏数
     */
    @TableField("favorites_count")
    private Integer favoritesCount;

    /**
     * 发帖数
     */
    @TableField("posts_count")
    private Integer postsCount;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
