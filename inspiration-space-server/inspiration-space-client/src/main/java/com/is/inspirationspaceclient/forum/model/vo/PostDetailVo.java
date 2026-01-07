package com.is.inspirationspaceclient.forum.model.vo;

import com.is.inspirationspaceclient.forum.model.entity.enums.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "帖子详情")
public class PostDetailVo implements Serializable {
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

    @Schema(name = "imageUrls",type = "List",description = "图片URL数组")
    private List<String> imageUrls;

    @Schema(name = "productUrl",type = "String",description = "商品跳转链接")
    private String productUrl;

    @Schema(name = "category", type = "PostCategory", description = "帖子分类")
    private PostCategory category;

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

    @Schema(name = "commentCount",type = "Integer",description = "评论数")
    private Integer commentCount;

    @Schema(name="isLiked",type = "Boolean",description = "是否已点赞")
    private Boolean isLiked;

    @Schema(name="isCollected",type = "Boolean",description = "是否已收藏")
    private Boolean isCollected;

    @Schema(name="isReposted",type = "Boolean",description = "是否已转发")
    private Boolean isReposted;
}
