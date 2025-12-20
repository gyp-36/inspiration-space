package com.is.inspirationspaceclient.forum.model.dto;

import com.is.inspirationspaceclient.work.model.entity.enums.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ForumCreateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "title", type = "String", description = "帖子标题")
    @NotEmpty
    private String title;

    @Schema(name = "content", type = "String", description = "帖子内容")
    @NotEmpty
    private String content;

    @Schema(name = "imageUrls", type = "List<String>", description = "图片URL数组")
    private List<String> imageUrls;

    @Schema(name = "productUrl", type = "String", description = "商品跳转链接")
    private String productUrl;

    @Schema(name = "visibility", type = "Integer", description = "可见范围(0公开，1私密，2好友可见)")
    private Visibility visibility;


}
