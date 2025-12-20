package com.is.inspirationspaceclient.work.model.dto;

import com.is.inspirationspaceclient.work.model.entity.enums.AccessStrategy;
import com.is.inspirationspaceclient.work.model.entity.enums.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "作品信息")
public class WorkCreateDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(name = "title", type = "String", description = "标题")
    @NotNull
    private String title;

    @Schema(name = "type", type = "String", description = "类型")
    @NotNull
    private String type;

    @Schema(name = "description", type = "String", description = "描述")
    private String description;

    @Schema(name = "coverUrl", type = "String", description = "封面图片URL")
    private String coverUrl;

    @Schema(name = "content", type = "String", description = "内容")
    private String content;

    @Schema(name = "price", type = "BigDecimal", description = "价格")
    private BigDecimal price;

    @Schema(name = "accessStrategy", type = "AccessStrategy", description = "访问策略")
    @NotNull
    private AccessStrategy accessStrategy;

    @Schema(name = "visibility", type = "Visibility", description = "可见性")
    @NotNull
    private Visibility visibility;

    @Schema(name = "attachmentIds", type = "List<Long>", description = "附件ID列表")
    private List<Long> attachmentIds;

}
