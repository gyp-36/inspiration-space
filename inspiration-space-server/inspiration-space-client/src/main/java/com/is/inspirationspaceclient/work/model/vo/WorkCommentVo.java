package com.is.inspirationspaceclient.work.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkCommentVo {
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Boolean isLiked;
}
