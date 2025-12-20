package com.is.inspirationspaceclient.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(title = "UserLoginVo", description = "用户登录信息")
public class UserLoginVo implements Serializable {

    @Schema(name = "userId", type = "long", description = "用户ID")
    private Long userId;

    @Schema(name = "token", type = "String", description = "用户登录token")
    private String token;


}
