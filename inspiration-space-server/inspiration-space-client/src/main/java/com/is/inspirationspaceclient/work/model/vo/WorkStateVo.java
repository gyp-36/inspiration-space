package com.is.inspirationspaceclient.work.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "作品日排行榜信息")
public class WorkStateVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name= "title",type = "String",description = "作品标题")
    private String title;

    @Schema(name = "totalSales", type = "Integer", description = "总销量")
    private Integer totalSales;
}
