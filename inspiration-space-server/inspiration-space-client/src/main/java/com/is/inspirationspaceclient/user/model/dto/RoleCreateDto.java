package com.is.inspirationspaceclient.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(title = "RoleCreateDto", description = "创建角色信息")
public class RoleCreateDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(name = "role_code", type = "String", description = "角色编码")
    private String roleCode;

    @Schema(name = "role_name", type = "String", description = "角色名称")
    private String roleName;

    @Schema(name = "role_description", type = "String", description = "角色描述")
    private String roleDescription;
}
