package com.is.inspirationspaceclient.work.model.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.is.inspirationspaceclient.work.model.entity.enums.AccessStrategy;
import com.is.inspirationspaceclient.work.model.entity.enums.Status;
import com.is.inspirationspaceclient.work.model.entity.enums.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "作品详情")
public class WorkDetailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "workId", type = "Long", description = "作品ID")
    private Long workId;

    @Schema(name = "authorId", type = "Long", description = "作者ID")
    private Long authorId;

    @Schema(name = "title", type = "String", description = "标题")
    private String title;

    @Schema(name = "type", type = "String", description = "类型")
    private String type;

    @Schema(name = "description", type = "String", description = "描述")
    private String description;

    @Schema(name = "coverUrl", type = "String", description = "封面URL")
    private String coverUrl;

    @Schema(name = "content", type = "String", description = "内容")
    private String content;

    @Schema(name = "price", type = "BigDecimal", description = "价格")
    private BigDecimal price;

    @Schema(name = "accessStrategy", type = "AccessStrategy", description = "访问策略")
    private AccessStrategy accessStrategy;

    @Schema(name = "status", type = "Status", description = "状态")
    private Status status;

    @Schema(name = "visibility", type = "Visibility", description = "可见性")
    private Visibility visibility;

    @Schema(name = "publishedAt", type = "LocalDateTime", description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(name = "updatedAt", type = "LocalDateTime", description = "更新时间")
    private LocalDateTime updatedAt;


    @Schema(name = "totalRevenue", type = "BigDecimal", description = "总收入")
    private BigDecimal totalRevenue;

    @Schema(name = "totalSales", type = "Integer", description = "总销量")
    private Integer totalSales;

    @Schema(name = "authorName", type = "String", description = "作者用户名")
    private String authorName;

    @Schema(name = "authorAvatar", type = "String", description = "作者头像URL")
    private String authorAvatar;

    @Schema(name = "viewCount", type = "Integer", description = "浏览量")
    private Integer viewCount;

    @Schema(name = "likeCount", type = "Integer", description = "点赞数")
    private Integer likeCount;

    @Schema(name = "collectCount", type = "Integer", description = "收藏数")
    private Integer collectCount;

    @Schema(name = "commentCount", type = "Integer", description = "评论数")
    private Integer commentCount;

    @Schema(name = "purchaseCount", type = "Integer", description = "购买数")
    private Integer purchaseCount;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "是否已收藏")
    private Boolean isCollected;

    @Schema(description = "是否已购买")
    private Boolean isPurchased;
}
