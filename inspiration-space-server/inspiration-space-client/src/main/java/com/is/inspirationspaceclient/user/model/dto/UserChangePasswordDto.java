package com.is.inspirationspaceclient.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(title = "UserChangePasswordDto", description = "用户界面用户修改密码")
public class UserChangePasswordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "userId", type = "Long", description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long userId;

    @Schema(name = "oldPassword", type = "String", description = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String oldPassword;

    @Schema(name = "newPassword", type = "String", description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String newPassword;


}
