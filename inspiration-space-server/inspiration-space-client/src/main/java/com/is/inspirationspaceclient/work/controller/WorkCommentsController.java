package com.is.inspirationspaceclient.work.controller;

import com.is.inspirationspaceclient.work.model.dto.WorkCommentDto;
import com.is.inspirationspaceclient.work.model.vo.WorkCommentVo;
import com.is.inspirationspaceclient.work.service.WorkCommentsService;
import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "作品评论接口")
@RestController
@RequestMapping("/works-comments")
public class WorkCommentsController {

    @Autowired
    private WorkCommentsService workCommentsService;

    @Operation(summary = "创建作品评论")
    @PostMapping("/create")
    public ApiResponse<Boolean> createComment(@RequestBody WorkCommentDto commentDto) {
        return ApiResponse.ok(workCommentsService.createComment(commentDto));
    }

    @Operation(summary = "获取作品评论列表")
    @GetMapping("/list/{workId}")
    public ApiResponse<List<WorkCommentVo>> getComments(
            @PathVariable Long workId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return ApiResponse.ok(workCommentsService.getCommentsByWorkId(token, workId));
    }

    @Operation(summary = "点赞/取消点赞作品评论")
    @PostMapping("/like/{commentId}")
    public ApiResponse<Boolean> toggleCommentLike(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(workCommentsService.toggleCommentLike(token, commentId));
    }
}
