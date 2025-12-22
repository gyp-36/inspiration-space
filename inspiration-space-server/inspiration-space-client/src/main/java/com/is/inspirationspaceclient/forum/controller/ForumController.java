package com.is.inspirationspaceclient.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.is.inspirationspaceclient.forum.model.dto.CommentDto;
import com.is.inspirationspaceclient.forum.model.dto.ForumCreateDto;
import com.is.inspirationspaceclient.forum.model.vo.CommentVo;
import com.is.inspirationspaceclient.forum.model.vo.PostDetailVo;
import com.is.inspirationspaceclient.forum.model.vo.PostSimpleVo;
import com.is.inspirationspaceclient.forum.service.ForumCommentService;
import com.is.inspirationspaceclient.forum.service.ForumService;

import com.is.inspirationspacecommon.util.ApiResponse;
import com.is.inspirationspacecommon.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/forum")
@Tag(name = "论坛控制器", description = "控制论坛")
public class ForumController {

    private final ForumService forumService;
    private final ForumCommentService forumCommentService;

    public ForumController(ForumService forumService, ForumCommentService forumCommentService) {
        this.forumService = forumService;
        this.forumCommentService = forumCommentService;
    }

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public ApiResponse<String> uploadImage(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(forumService.uploadImage(token, file));
    }

    @Operation(summary = "发布帖子")
    @PostMapping("/create")
    public ApiResponse<Boolean> createPost(

            @RequestHeader("Authorization") String token,
            @RequestBody @Valid ForumCreateDto forumCreateDto) {
        return ApiResponse.ok(forumService.createPost(token,forumCreateDto));
    }

    @Operation(summary = "删除帖子")
    @PostMapping("/delete")
    public ApiResponse<Boolean> deletePost(@RequestParam Long postId) {
        return ApiResponse.ok(forumService.deletePost(postId));
    }

    @Operation(summary = "主页获取所有帖子(按sort区分)")
    @GetMapping("/getAll")
    public ApiResponse<Page<PostSimpleVo>> getAllPosts(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(forumService.getAllPosts(token, sort, keyword, page, size));
    }

    @Operation(summary = "获取收藏的帖子")
    @GetMapping("/getCollected")
    public ApiResponse<Page<PostSimpleVo>> getCollectedPosts(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ApiResponse.ok(forumService.getCollectedPosts(token, page, size));
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/getDetail/{postId}")
    public ApiResponse<PostDetailVo> getPostDetail(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long postId) {
        return ApiResponse.ok(forumService.getPostDetail(token, postId));
    }

    @Operation(summary = "发布评论")
    @PostMapping("/comment")
    public ApiResponse<Boolean> createComment(
            @RequestHeader("Authorization") String token,
            @RequestBody CommentDto commentDto) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        commentDto.setUserId(userId);
        return ApiResponse.ok(forumCommentService.createComment(commentDto));
    }

    @Operation(summary = "删除评论")
    @PostMapping("/deleteComment")
    public ApiResponse<Boolean> deleteComment(@Schema(description = "评论ID") Long commentId) {
        return ApiResponse.ok(forumCommentService.deleteComment(commentId));
    }

    @Operation(summary = "获取帖子评论")
    @GetMapping("/getComments/{postId}")
    public ApiResponse<List<CommentVo>> getComments(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long postId) {
        return ApiResponse.ok(forumCommentService.getCommentsByPostId(token, postId));
    }

    @Operation(summary = "点赞评论")
    @PostMapping("/likeComment")
    public ApiResponse<Boolean> likeComment(
            @RequestHeader("Authorization") String token,
            @RequestParam Long commentId) {
        return ApiResponse.ok(forumCommentService.likeComment(token, commentId));
    }

    @Operation(summary = "取消点赞评论")
    @PostMapping("/unlikeComment")
    public ApiResponse<Boolean> unlikeComment(
            @RequestHeader("Authorization") String token,
            @RequestParam Long commentId) {
        return ApiResponse.ok(forumCommentService.unlikeComment(token, commentId));
    }

    @Operation(summary = "点赞")
    @PostMapping("/like")
    public ApiResponse<Boolean> like(@RequestHeader("Authorization") String token,@RequestParam Long postId) {
        return ApiResponse.ok(forumService.like(token,postId));
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/unlike")
    public ApiResponse<Boolean> unlike(@RequestHeader("Authorization") String token,@RequestParam Long postId) {
        return ApiResponse.ok(forumService.unlike(token,postId));
    }

    @Operation(summary = "转发")
    @PostMapping("/repost")
    public ApiResponse<Boolean> repost(@RequestHeader("Authorization") String token,@RequestParam Long postId) {
        return ApiResponse.ok(forumService.repost(token,postId));
    }

    @Operation(summary = "收藏")
    @PostMapping("/collect")
    public ApiResponse<Boolean> collect(@RequestHeader("Authorization") String token,@RequestParam Long postId) {
        return ApiResponse.ok(forumService.collect(token,postId));
    }

    @Operation(summary = "取消收藏")
    @PostMapping("/uncollect")
    public ApiResponse<Boolean> uncollect(@RequestHeader("Authorization") String token,@RequestParam Long postId) {
        return ApiResponse.ok(forumService.uncollect(token,postId));
    }






//    @Operation(summary = "按用户名查询帖子")
//    @GetMapping("/getByUser")
//    public ApiResponse<List<Long>> getPostsByUser(@Schema(description = "用户名") String userName) {
//        return ApiResponse.ok(forumService.getPostsByUser(userName));
//    }


}
