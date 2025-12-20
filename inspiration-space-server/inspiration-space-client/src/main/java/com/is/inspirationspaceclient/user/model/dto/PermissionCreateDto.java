package com.is.inspirationspaceclient.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "权限创建参数")
public class PermissionCreateDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "权限码")
    private String permissionCode;

    @Schema(description = "权限名")
    private String permissionName;

    @Schema(description = "权限描述")
    private String description;
}
