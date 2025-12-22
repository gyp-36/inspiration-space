package com.is.inspirationspaceclient.chat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.is.inspirationspaceclient.chat.model.entity.enums.IsApproval;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "创建群聊请求参数")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateGroupRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "groupName", type = "String", description = "群聊名称")
    private String groupName;

   @Schema(name = "description", type = "String", description = "群聊描述")
    private String description;

   @Schema(name = "requiredApproval", type = "IsApproval", description = "入群是否需审核")
    private IsApproval requiredApproval;


}