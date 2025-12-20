package com.is.inspirationspaceclient.forum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.is.inspirationspaceclient.forum.model.entity.enums.TargetType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户行为记录表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("forum_user_actions")
public class ForumUserActions {
    
    /**
     * 行为记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 目标类型(1评论，2点赞，3收藏，4转发)
     */
    @TableField("target_type")
    private TargetType targetType;
    
    /**
     * 目标ID
     */
    @TableField("target_id")
    private Long targetId;
    
    /**
     * 是否有效：1=激活，0=已取消
     */
    @TableField("is_active")
    private Integer isActive;
    
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