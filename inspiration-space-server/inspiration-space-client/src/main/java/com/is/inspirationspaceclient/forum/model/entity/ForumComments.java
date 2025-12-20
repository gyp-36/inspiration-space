package com.is.inspirationspaceclient.forum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 评论表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("forum_comments")
public class ForumComments {
    
    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;
    
    /**
     * 评论人ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 父评论ID，NULL表示直接评论帖子
     */
    @TableField("parent_comment_id")
    private Long parentCommentId;
    
    /**
     * 评论内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;
    
    /**
     * 软删除标记
     */
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}