package com.is.inspirationspaceclient.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.is.inspirationspaceclient.forum.mapper.ForumCommentsMapper;
import com.is.inspirationspaceclient.forum.mapper.ForumPostsMapper;
import com.is.inspirationspaceclient.forum.mapper.ForumUserActionsMapper;
import com.is.inspirationspaceclient.forum.model.dto.CommentDto;
import com.is.inspirationspaceclient.forum.model.entity.ForumComments;
import com.is.inspirationspaceclient.forum.model.entity.ForumUserActions;
import com.is.inspirationspaceclient.forum.model.entity.enums.TargetType;
import com.is.inspirationspaceclient.forum.model.vo.CommentVo;
import com.is.inspirationspaceclient.user.service.UserService;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForumCommentServiceImpl implements ForumCommentService {

    private final ForumCommentsMapper forumCommentsMapper;
    private final ForumUserActionsMapper forumUserActionsMapper;
    private final ForumService forumService;
    private final UserService userService;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createComment(CommentDto commentDto) {
        if (commentDto == null || commentDto.getPostId() == null || commentDto.getContent() == null) {
            return false;
        }
        
        // 1. 插入评论
        ForumComments comment = new ForumComments();
        comment.setId(snowflakeIdGenerator.nextId());
        comment.setPostId(commentDto.getPostId());
        comment.setContent(commentDto.getContent());
        comment.setUserId(commentDto.getUserId());
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        
        boolean inserted = forumCommentsMapper.insert(comment) > 0;
        if (!inserted) return false;

        // 2. 记录用户行为 (评论)
        ForumUserActions existingAction = forumUserActionsMapper.selectOne(
            Wrappers.lambdaQuery(ForumUserActions.class)
                .eq(ForumUserActions::getUserId, commentDto.getUserId())
                .eq(ForumUserActions::getTargetId, commentDto.getPostId())
                .eq(ForumUserActions::getTargetType, TargetType.COMMENT)
        );
        
        if (existingAction == null) {
            ForumUserActions action = new ForumUserActions();
            action.setUserId(commentDto.getUserId());
            action.setTargetId(commentDto.getPostId());
            action.setTargetType(TargetType.COMMENT);
            action.setIsActive(1);
            action.setCreatedAt(LocalDateTime.now());
            action.setUpdatedAt(LocalDateTime.now());
            forumUserActionsMapper.insert(action);
        } else if (existingAction.getIsActive() != 1) {
            existingAction.setIsActive(1);
            existingAction.setUpdatedAt(LocalDateTime.now());
            forumUserActionsMapper.updateById(existingAction);
        }

        // 3. 更新帖子统计信息 (评论数+1)
        forumService.updatePostStatsAsync(commentDto.getPostId(), "comment_count", 1);
        
        return true;
    }

    @Override
    public Boolean deleteComment(Long commentId) {
        return forumCommentsMapper.deleteById(commentId) > 0;
    }

    @Override
    public List<CommentVo> getCommentsByPostId(String token, Long postId) {
        LambdaQueryWrapper<ForumComments> queryWrapper = Wrappers.lambdaQuery(ForumComments.class)
                .eq(ForumComments::getPostId, postId)
                .orderByDesc(ForumComments::getCreatedAt);
        
        List<ForumComments> comments = forumCommentsMapper.selectList(queryWrapper);
        
        // 获取当前用户ID
        Long currentUserId = null;
        if (token != null && !token.isEmpty() && !"null".equals(token)) {
            try {
                currentUserId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                log.warn("Invalid token in getCommentsByPostId: {}", e.getMessage());
            }
        }

        final Long userId = currentUserId;

        return comments.stream().map(comment -> {
            CommentVo vo = new CommentVo();
            vo.setId(comment.getId());
            vo.setUserId(comment.getUserId());
            vo.setContent(comment.getContent());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setLikeCount(comment.getLikeCount());
            
            // 获取用户信息
            try {
                var userInfo = userService.getUserInfo(comment.getUserId());
                if (userInfo != null) {
                    vo.setUsername(userInfo.getUserName());
                    vo.setAvatar(userService.getAvatar(comment.getUserId()));
                }
            } catch (Exception e) {
                vo.setUsername("用户" + comment.getUserId());
            }

            // 检查当前用户是否已点赞该评论
            if (userId != null) {
                ForumUserActions action = forumUserActionsMapper.selectOne(Wrappers.lambdaQuery(ForumUserActions.class)
                        .eq(ForumUserActions::getUserId, userId)
                        .eq(ForumUserActions::getTargetId, comment.getId())
                        .eq(ForumUserActions::getTargetType, TargetType.LIKE_COMMENT)
                        .eq(ForumUserActions::getIsActive, 1));
                vo.setIsLiked(action != null);
            } else {
                vo.setIsLiked(false);
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean likeComment(String token, Long commentId) {
        return toggleCommentLike(token, commentId, 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlikeComment(String token, Long commentId) {
        return toggleCommentLike(token, commentId, 0);
    }

    private Boolean toggleCommentLike(String token, Long commentId, int targetStatus) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 1. 检查评论是否存在
        ForumComments comment = forumCommentsMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }

        // 2. 检查或创建行为记录
        ForumUserActions action = forumUserActionsMapper.selectOne(Wrappers.lambdaQuery(ForumUserActions.class)
                .eq(ForumUserActions::getUserId, userId)
                .eq(ForumUserActions::getTargetId, commentId)
                .eq(ForumUserActions::getTargetType, TargetType.LIKE_COMMENT));

        if (action != null) {
            if (action.getIsActive() == targetStatus) {
                return true;
            }
            action.setIsActive(targetStatus);
            forumUserActionsMapper.updateById(action);
        } else {
            if (targetStatus == 1) {
                action = new ForumUserActions();
                action.setUserId(userId);
                action.setTargetId(commentId);
                action.setTargetType(TargetType.LIKE_COMMENT);
                action.setIsActive(1);
                action.setCreatedAt(LocalDateTime.now());
                action.setUpdatedAt(LocalDateTime.now());
                forumUserActionsMapper.insert(action);
            } else {
                return false;
            }
        }

        // 3. 更新评论点赞数
        int delta = (targetStatus == 1) ? 1 : -1;
        forumCommentsMapper.updateLikeCount(commentId, delta);

        return true;
    }
}
