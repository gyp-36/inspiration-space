package com.is.inspirationspaceclient.work.controller;


import com.is.inspirationspaceclient.work.model.vo.WorkRankVo;
import com.is.inspirationspaceclient.work.service.WorkStateService;
import com.is.inspirationspacecommon.util.ApiResponse;
import com.is.inspirationspacecommon.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/works/stats")
@Tag(name = "作品统计接口", description = "作品统计相关操作")
public class WorkStatesController {
    @Autowired
    private WorkStateService workStateService;

    @Operation(summary = "增加作品浏览數")
    @PostMapping("/{workId}/view")
    public ApiResponse<Void> incrementViewCount(
            @PathVariable Long workId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            userId = JwtUtil.getUserIdFromToken(token);
        }
        workStateService.incrementViewCount(workId, userId);
        return ApiResponse.ok();
    }

    @Operation(summary = "切换作品点赞状态")
    @PostMapping("/{workId}/toggle-like")
    public ApiResponse<Void> toggleLikeCount(
            @PathVariable Long workId,
            @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        workStateService.toggleLike(workId, userId);
        return ApiResponse.ok();
    }

    @Operation(summary = "切换作品收藏状态")
    @PostMapping("/{workId}/toggle-collect")
    public ApiResponse<Void> toggleCollectCount(
            @PathVariable Long workId,
            @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        workStateService.toggleCollect(workId, userId);
        return ApiResponse.ok();
    }
    @Operation(summary = "增加作品评论数")
    @PostMapping("/{workId}/comment")
    public ApiResponse<Void> incrementCommentCount(@PathVariable Long workId) {
        workStateService.incrementCommentCount(workId);
        return ApiResponse.ok();
    }

    @Operation(summary = "增加购买数")
    @PostMapping("/{workId}/purchase")
    public ApiResponse<Void> incrementPurchaseCount(@PathVariable Long workId) {
        workStateService.incrementPurchaseCount(workId);
        return ApiResponse.ok();
    }

//    @Operation(summary = "获取单个作品的总统计信息")
//    @GetMapping("/{workId}/detail")
//    public ApiResponse<WorkStateDetailVo> getWorkStateDetail(@PathVariable Long workId) {
//        return ApiResponse.ok(workStateService.getWorkStateDetail(workId));
//    }
//
//    @Operation(summary = "获取所有作品总统计信息")
//    @GetMapping("/all")
//    public ApiResponse<WorkStateDetailVo> getAllWorkState() {
//        return ApiResponse.ok(workStateService.getAllWorkState());
//    }

    @Operation(summary = "获取作品点赞排行榜")
    @GetMapping("/rank")
    public ApiResponse<List<WorkRankVo>> getWorkLikeRank(
            @RequestParam(defaultValue = "10") int topN) {
        List<WorkRankVo> result=workStateService.getWorkLikeRank(topN);
        return ApiResponse.ok(result);

    }

    @Operation(summary = "获取作品畅销排行榜")
    @GetMapping("/sales")
    public ApiResponse<List<WorkRankVo>> getWorkSalesRank(
            @RequestParam(defaultValue = "10") int topN) {
        List<WorkRankVo> result =workStateService.getWorkSalesRank(topN);
        return ApiResponse.ok(result);

    }

}