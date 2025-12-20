package com.is.inspirationspaceclient.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(title = "UserRegisterDto", description = "用户注册")
public class UserRegisterDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "username", type = "String", description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String username;

    @Schema(name = "password", type = "String", description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String password;

    @Schema(name = "email", type = "String", description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String email;

    @Schema(name = "phone", type = "String", description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String phone;


}
