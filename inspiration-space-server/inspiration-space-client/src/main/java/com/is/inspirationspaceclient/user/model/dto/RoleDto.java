package com.is.inspirationspaceclient.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(title = "RoleDto", description = "角色信息")
public class RoleDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "roleId", type = "Long", description = "角色ID")
    private Long roleId;

    @Schema(name = "role_name", type = "String", description = "角色名称")
    private String roleName;

    @Schema(name = "role_description", type = "String", description = "角色描述")
    private String roleDescription;
}
