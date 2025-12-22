package com.is.inspirationspaceclient.work.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.is.inspirationspaceclient.work.model.entity.enums.AccessStrategy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作品附件实体类
 */
@Data
@TableName("work_attachment")
public class WorkAttachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 附件ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 作品ID
     */
    @TableField("work_id")
    private Long workId;

    /**
     * 原始文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件扩展名（如.pdf, .mp4）
     */
    @TableField("file_extension")
    private String fileExtension;

    /**
     * MIME类型（如application/pdf）
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件哈希值（SHA-256）
     */
    @TableField("file_hash")
    private String fileHash;

    /**
     * MinIO bucket名称
     */
    @TableField("bucket_name")
    private String bucketName ;

    /**
     * MinIO对象键
     */
    @TableField("object_key")
    private String objectKey;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 访问策略：0=公开, 1=会员, 2=付费
     */
    @TableField("access_strategy")
    private AccessStrategy accessStrategy;

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