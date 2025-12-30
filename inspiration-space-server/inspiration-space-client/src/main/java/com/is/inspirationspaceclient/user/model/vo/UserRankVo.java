package com.is.inspirationspaceclient.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
@Data
@Schema(title = "UserRankVo", description = "用户排行信息")
public class UserRankVo implements Serializable {
    @Schema(name = "id", type = "Long", description = "用户id")
    private Long userId;

    @Schema(name = "username", type = "String", description = "用户名")
    private String username;

    @Schema(name = "avatar", type = "String", description = "头像")
    private String avatar;

    @Schema(name="likes_count", type = "Integer", description = "点赞数")
    private Integer likesCount;
}
