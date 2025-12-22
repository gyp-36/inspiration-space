package com.is.inspirationspaceclient.work.service;

import com.is.inspirationspaceclient.work.model.dto.WorkCommentDto;
import com.is.inspirationspaceclient.work.model.vo.WorkCommentVo;
import java.util.List;

public interface WorkCommentsService {
    Boolean createComment(WorkCommentDto commentDto);
    List<WorkCommentVo> getCommentsByWorkId(String token, Long workId);
    Boolean toggleCommentLike(String token, Long commentId);
}
