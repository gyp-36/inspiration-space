package com.is.inspirationspaceclient.notification.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification_receivers")
public class NotificationReceiver {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("notification_id")
    private Long notificationId;
    
    @TableField("receiver_id")
    private Long receiverId;
    
    @TableField("read_status")
    private Integer readStatus;
    
    @TableField("read_at")
    private LocalDateTime readAt;
}