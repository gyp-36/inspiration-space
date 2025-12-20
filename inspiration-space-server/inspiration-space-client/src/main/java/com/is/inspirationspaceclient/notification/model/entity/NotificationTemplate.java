package com.is.inspirationspaceclient.notification.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification_templates")
public class NotificationTemplate {
    
    @TableId(value = "template_id", type = IdType.AUTO)
    private Integer templateId;
    
    @TableField("template_name")
    private String templateName;
    
    @TableField("template_content")
    private String templateContent;
    
    @TableField("variables")
    private String variables; // JSON格式字符串
    
    @TableField("template_category")
    private Integer templateCategory;
    
    @TableField(value = "create_at", fill = FieldFill.INSERT)
    private LocalDateTime createAt;
    
    @TableField(value = "update_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateAt;
    
    @TableField("status")
    private Integer status;
}