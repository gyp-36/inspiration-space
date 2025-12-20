package com.is.inspirationspaceclient.forum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 帖子统计表实体类
 */
@Data
@Accessors(chain = true)
@TableName("forum_post_stats")
public class ForumPostStats {
    
    /**
     * 帖子ID
     */
    @TableId(value = "post_id")
    private Long postId;
    
    /**
     * 点赞总数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 收藏总数
     */
    @TableField("collect_count")
    private Integer collectCount;
    
    /**
     * 评论总数
     */
    @TableField("comment_count")
    private Integer commentCount;
    
    /**
     * 转发总数
     */
    @TableField("repost_count")
    private Integer repostCount;
    
    /**
     * 浏览量
     */
    @TableField("view_count")
    private Integer viewCount;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}