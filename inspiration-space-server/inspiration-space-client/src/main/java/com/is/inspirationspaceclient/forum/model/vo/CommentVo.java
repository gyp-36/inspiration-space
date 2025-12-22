package com.is.inspirationspaceclient.forum.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "评论详情")
public class CommentVo {
    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "发布时间")
    private LocalDateTime createdAt;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;
}

