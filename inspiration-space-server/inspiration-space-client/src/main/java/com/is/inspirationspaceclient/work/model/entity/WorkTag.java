package com.is.inspirationspaceclient.work.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作品标签表
 */
@Data
@TableName("work_tag")
public class WorkTag {

    @TableId("work_id")
    private Long workId;

    @TableField("tag_id")
    private Long tagId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
