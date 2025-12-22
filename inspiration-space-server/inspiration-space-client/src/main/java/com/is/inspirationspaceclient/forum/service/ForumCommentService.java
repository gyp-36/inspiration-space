package com.is.inspirationspaceclient.forum.service;


import com.is.inspirationspaceclient.forum.model.dto.CommentDto;
import com.is.inspirationspaceclient.forum.model.vo.CommentVo;
import java.util.List;

public interface ForumCommentService {


    Boolean createComment(CommentDto commentDto);

    Boolean deleteComment(Long commentId);

    List<CommentVo> getCommentsByPostId(String token, Long postId);

    Boolean likeComment(String token, Long commentId);

    Boolean unlikeComment(String token, Long commentId);
}
