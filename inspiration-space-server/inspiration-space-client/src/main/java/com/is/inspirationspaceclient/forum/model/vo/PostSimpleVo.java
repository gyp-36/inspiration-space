package com.is.inspirationspaceclient.forum.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PostSimpleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "postId",type = "Long",description = "帖子ID")
    private Long postId;

    @Schema(name = "userId",type = "Long",description = "用户ID")
    private Long userId;

    @Schema(name = "title",type = "String",description = "帖子标题")
    private String title;

    @Schema(name = "content",type = "String",description = "帖子内容")
    private String content;

    @Schema(name = "create_at",type = "LocalDateTime",description = "创建时间")
    private LocalDateTime createAt;

    @Schema(name="like",type = "Integer",description = "点赞数")
    private Integer like;

    @Schema(name="collect",type = "Integer",description = "收藏数")
    private Integer collect;

    @Schema(name="repost",type = "Integer",description = "转发数")
    private Integer repost;

    @Schema(name="view",type = "Integer",description = "浏览数")
    private Integer view;



}
