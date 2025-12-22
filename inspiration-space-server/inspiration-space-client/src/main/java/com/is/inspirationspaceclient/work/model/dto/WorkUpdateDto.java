package com.is.inspirationspaceclient.work.model.dto;

import com.is.inspirationspaceclient.work.model.entity.enums.AccessStrategy;
import com.is.inspirationspaceclient.work.model.entity.enums.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "作品更新信息")
public class WorkUpdateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "title", type = "String", description = "标题")
    private String title;

    @Schema(name = "type", type = "String", description = "类型")
    private String type;

    @Schema(name = "description", type = "String", description = "描述")
    private String description;

    @Schema(name = "coverUrl", type = "String", description = "封面图片URL")
    private String coverUrl;

    @Schema(name = "content", type = "String", description = "内容")
    private String content;

    @Schema(name = "accessStrategy", type = "AccessStrategy", description = "访问策略")
    private AccessStrategy accessStrategy;

    @Schema(name = "visibility", type = "Visibility", description = "可见性")
    private Visibility visibility;

    @Schema(name = "attachmentIds", type = "List<Long>", description = "附件ID列表")
    private List<Long> attachmentIds;

    @Schema(name = "tags", type = "List<String>", description = "标签名称列表")
    private List<String> tags;

}
