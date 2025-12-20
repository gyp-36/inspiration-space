package com.is.inspirationspaceclient.notification.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {

    
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;
    
    @TableField("sender_id")
    private Long senderId;
    
    @TableField("message_type")
    private Integer messageType;
    
    @TableField("template_id")
    private Integer templateId;
    
    @TableField("content")
    private String content;
    
    @TableField("category")
    private Integer category;
    
    @TableField("status")
    private Integer status;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "update_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateAt;
    
    @TableField("scheduled_at")
    private LocalDateTime scheduledAt;
}