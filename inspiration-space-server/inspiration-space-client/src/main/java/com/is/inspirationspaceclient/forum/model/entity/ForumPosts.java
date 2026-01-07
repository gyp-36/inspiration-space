package com.is.inspirationspaceclient.forum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import com.is.inspirationspaceclient.forum.model.entity.enums.PostCategory;
import com.is.inspirationspaceclient.forum.model.entity.enums.Visibility;
import lombok.Data;

import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子主表实体类
 */
@Data
@Accessors(chain = true)
@TableName(value = "forum_posts",autoResultMap = true)
public class ForumPosts {
    
    /**
     * 帖子ID
     */
    @TableId(value = "post_id")
    private Long postId;
    
    /**
     * 发帖人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 帖子标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 帖子内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 图片URL数组
     */
    @TableField(value = "image_urls", typeHandler = JacksonTypeHandler.class)
    private List<String> imageUrls;
    
    /**
     * 商品跳转链接
     */
    @TableField("product_url")
    private String productUrl;
    
    /**
     * 可见范围(0公开，1私密，2好友可见)
     */
    @TableField("visibility")
    private Visibility visibility;

    /**
     * 帖子分类
     */
    @TableField("category")
    private PostCategory category;
    
    /**
     * 软删除：0=正常，1=已删
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