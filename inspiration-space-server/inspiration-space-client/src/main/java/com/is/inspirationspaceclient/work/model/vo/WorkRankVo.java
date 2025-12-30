package com.is.inspirationspaceclient.work.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "作品日排行榜信息")
public class WorkRankVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "workId", type = "Long", description = "作品ID")
    private Long workId;

    @Schema(name = "cover", type = "String", description = "封面")
    private String cover;

    @Schema(name = "author", type = "String", description = "作者")
    private String author;

    @Schema(name = "userId", type = "Long", description = "作者ID")
    private Long userId;

    @Schema(name= "title",type = "String",description = "作品标题")
    private String title;

    @Schema(name = "like_count", type = "Integer", description = "总点赞数")
    private Integer likeCount;

    @Schema(name = "total_sales", type = "Integer", description = "总销量")
    private Integer totalSales;
}
