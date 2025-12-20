package com.is.inspirationspaceclient.forum.service;


import com.is.inspirationspaceclient.forum.model.dto.CommentDto;

public interface ForumCommentService {


    Boolean createComment(CommentDto commentDto);

    Boolean deleteComment(Long commentId);
}