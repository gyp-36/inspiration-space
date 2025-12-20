package com.is.inspirationspaceclient.work.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.is.inspirationspaceclient.work.model.entity.enums.TagType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签表
 */
@Data
@TableName("tag")
public class Tag implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    @TableId(value = "tag_id", type = IdType.AUTO)
    private Long tagId;

    /**
     * 标签名称
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * 标签描述
     */
    @TableField("tag_description")
    private String tagDescription;

    /**
     * 标签类型
     */
    @TableField("tag_type")
    private TagType tagType;

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
