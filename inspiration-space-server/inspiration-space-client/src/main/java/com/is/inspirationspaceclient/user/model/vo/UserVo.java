package com.is.inspirationspaceclient.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "UserVo", description = "用户信息")
public class UserVo {
    @Schema(name = "id", type = "Long", description = "用户id")
    private Long id;

    @Schema(name = "username", type = "String", description = "用户名")
    private String username;

    @Schema(name = "avatarUrl", type = "String", description = "头像URL")
    private String avatarUrl;

    @Schema(name = "bio", type = "String", description = "简介")
    private String bio;



    @Schema(name = "followings_count", type = "Integer", description = "关注数")
    private Integer followingsCount;

    @Schema(name = "fans_count", type = "Integer", description = "粉丝数")
    private Integer fansCount;

    @Schema(name = "likes_count", type = "Integer", description = "点赞数")
    private Integer likesCount;

    @Schema(name = "favorites_count", type = "Integer", description = "收藏数")
    private Integer favoritesCount;

    @Schema(name = "posts_count", type = "Integer", description = "帖子数")
    private Integer postsCount;



}
