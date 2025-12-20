package com.is.inspirationspaceclient.work.model.dto;

import com.is.inspirationspaceclient.work.model.entity.enums.TagType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "创建标签参数")
public class TagCreateDto implements Serializable {

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签类型")
    private TagType tagType;

    @Schema(description = "标签描述")
    private String tagDescription;


}
