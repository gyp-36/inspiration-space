package com.is.inspirationspaceclient.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "渲染帖子信息")
public class UserInfoVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "userName",type = "String",description = "用户名")
    private String userName;

    @Schema(name = "avatar",type = "String",description = "用户头像")
    private String avatar;
}
