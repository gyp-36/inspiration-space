package com.is.inspirationspaceclient.work.model.dto;

import lombok.Data;

@Data
public class WorkCommentDto {
    private Long workId;
    private Long userId;
    private String content;
    private Long parentCommentId;
}
