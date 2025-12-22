package com.is.inspirationspaceclient.work.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("work_comments")
public class WorkComments {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("work_id")
    private Long workId;

    @TableField("user_id")
    private Long userId;

    @TableField("parent_comment_id")
    private Long parentCommentId;

    @TableField("content")
    private String content;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
