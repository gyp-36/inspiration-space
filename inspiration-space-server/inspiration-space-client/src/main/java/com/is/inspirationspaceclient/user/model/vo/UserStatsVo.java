package com.is.inspirationspaceclient.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "UserStatsVo", description = "用户统计信息")
public class UserStatsVo {

    @Schema(name = "userId", type = "Long", description = "用户id")
    private Long userId;

    @Schema(name = "likes_count",type = "Integer", description = "点赞数")
    private Integer likesCount;

    @Schema(name = "fans_count",type = "Integer", description = "粉丝数")
    private Integer fansCount;

    @Schema(name = "followings_count",type = "Integer", description = "关注数")
    private Integer followingsCount;

    @Schema(name = "posts_count",type = "Integer", description = "帖子数")
    private Integer postsCount;
}

