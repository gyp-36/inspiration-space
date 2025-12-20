package com.is.inspirationspaceclient.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(title = "UserLoginDto", description = "用户登录")
public class UserLoginDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "username", type = "String", description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String username;

    @Schema(name = "password", type = "String", description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String password;

    @Schema(name = "captcha", type = "String", description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String captcha;
}
