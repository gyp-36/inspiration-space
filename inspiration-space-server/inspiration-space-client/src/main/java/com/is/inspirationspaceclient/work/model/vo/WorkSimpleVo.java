package com.is.inspirationspaceclient.work.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "作品列表")
public class WorkSimpleVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "title",type = "String",description = "标题")
    private String title;

    @Schema(name = "type",type = "String",description = "类型")
    private String type;

    @Schema(name = "coverUrl",type = "String",description = "封面图片URL")
    private String coverUrl;

    @Schema(name = "description",type = "String",description = "描述")
    private String description;

    @Schema(name = "price",type = "BigDecimal",description = "价格")
    private BigDecimal price;

    @Schema(name = "published_at",type = "LocalDateTime",description = "发布时间")
    private LocalDateTime publishedAt;

}
