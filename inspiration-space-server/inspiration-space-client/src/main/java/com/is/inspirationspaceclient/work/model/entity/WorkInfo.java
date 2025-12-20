package com.is.inspirationspaceclient.work.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.is.inspirationspaceclient.work.model.entity.enums.AccessStrategy;
import com.is.inspirationspaceclient.work.model.entity.enums.Status;
import com.is.inspirationspaceclient.work.model.entity.enums.Visibility;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * 作品表
 */
@Data
@TableName("work_info")
public class WorkInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 作品ID
     */
    @TableId(value = "work_id")
    private Long workId;

    /**
     * 作者ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 封面图片URL
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 访问策略
     */
    @TableField("access_strategy")
    private AccessStrategy accessStrategy;

    /**
     * 状态
     */
    @TableField("status")
    private Status status;

    /**
     * 可见性
     */
    @TableField("visibility")
    private Visibility visibility;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 发布时间
     */
    @TableField("published_at")
    private LocalDateTime publishedAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 总收入
     */
    @TableField("total_revenue")
    private BigDecimal totalRevenue;

    /**
     * 总销量
     */
    @TableField("total_sales")
    private Integer totalSales;
}