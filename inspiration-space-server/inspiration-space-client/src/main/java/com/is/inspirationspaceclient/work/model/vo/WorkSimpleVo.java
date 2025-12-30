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

    @Schema(name = "workId", type = "Long", description = "作品ID")
    private Long workId;

    @Schema(name = "title",type = "String",description = "标题")
    private String title;

    @Schema(name = "authorId", type = "Long", description = "作者ID")
    private Long authorId;

    @Schema(name = "authorName", type = "String", description = "作者名称")
    private String authorName;

    @Schema(name = "authorAvatar", type = "String", description = "作者头像")
    private String authorAvatar;

    @Schema(name = "type",type = "String",description = "类型")
    private String type;

    @Schema(name = "accessStrategy", type = "String", description = "访问策略")
    private String accessStrategy;

    @Schema(name = "coverUrl",type = "String",description = "封面图片URL")
    private String coverUrl;

    @Schema(name = "description",type = "String",description = "描述")
    private String description;

    @Schema(name = "price",type = "BigDecimal",description = "价格")
    private BigDecimal price;

    @Schema(name = "publishedAt",type = "LocalDateTime",description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(name = "viewCount", type = "Integer", description = "浏览量")
    private Integer viewCount;

    @Schema(name = "likeCount", type = "Integer", description = "点赞量")
    private Integer likeCount;

    @Schema(name = "collectCount", type = "Integer", description = "收藏量")
    private Integer collectCount;

    @Schema(name = "commentCount", type = "Integer", description = "评论数")
    private Integer commentCount;

    @Schema(name = "purchaseCount", type = "Integer", description = "购买数")
    private Integer purchaseCount;

    @Schema(name = "isLiked", type = "Boolean", description = "是否已点赞")
    private Boolean isLiked;

    @Schema(name = "isCollected", type = "Boolean", description = "是否已收藏")
    private Boolean isCollected;

    @Schema(name = "isPurchased", type = "Boolean", description = "是否已购买")
    private Boolean isPurchased;
}
