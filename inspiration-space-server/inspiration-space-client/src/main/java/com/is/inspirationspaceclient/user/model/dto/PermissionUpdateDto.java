package com.is.inspirationspaceclient.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限修改参数
 */
@Data
@Schema(description = "权限修改参数")
public class PermissionUpdateDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "permissionId", type = "Long", description = "权限ID")
    private Long permissionId;

    @Schema(name = "permissionName", type = "String", description = "权限名称")
    private String permissionName;

    @Schema(name = "description", type = "String", description = "权限描述")
    private String description;


}
