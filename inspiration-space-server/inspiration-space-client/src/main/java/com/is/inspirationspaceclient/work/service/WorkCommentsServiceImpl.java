package com.is.inspirationspaceclient.work.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.is.inspirationspaceclient.forum.mapper.ForumUserActionsMapper;
import com.is.inspirationspaceclient.forum.model.entity.ForumUserActions;
import com.is.inspirationspaceclient.forum.model.entity.enums.TargetType;
import com.is.inspirationspaceclient.user.service.UserService;
import com.is.inspirationspaceclient.work.mapper.WorkCommentsMapper;
import com.is.inspirationspaceclient.work.mapper.WorkStatsMapper;
import com.is.inspirationspaceclient.work.model.dto.WorkCommentDto;
import com.is.inspirationspaceclient.work.model.entity.WorkComments;
import com.is.inspirationspaceclient.work.model.vo.WorkCommentVo;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkCommentsServiceImpl implements WorkCommentsService {

    @Autowired
    private WorkCommentsMapper workCommentsMapper;

    @Autowired
    private ForumUserActionsMapper userActionsMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private WorkStateService workStateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createComment(WorkCommentDto commentDto) {
        WorkComments comment = new WorkComments();
        comment.setWorkId(commentDto.getWorkId());
        comment.setUserId(commentDto.getUserId());
        comment.setContent(commentDto.getContent());
        comment.setParentCommentId(commentDto.getParentCommentId());
        comment.setLikeCount(0);
        comment.setIsDeleted(0);
        
        int inserted = workCommentsMapper.insert(comment);
        if (inserted > 0) {
            // 更新统计数据
            workStateService.incrementCommentCount(commentDto.getWorkId());
            return true;
        }
        return false;
    }

    @Override
    public List<WorkCommentVo> getCommentsByWorkId(String token, Long workId) {
        LambdaQueryWrapper<WorkComments> queryWrapper = Wrappers.lambdaQuery(WorkComments.class)
                .eq(WorkComments::getWorkId, workId)
                .eq(WorkComments::getIsDeleted, 0)
                .orderByDesc(WorkComments::getCreatedAt);
        
        List<WorkComments> comments = workCommentsMapper.selectList(queryWrapper);
        
        Long currentUserId = null;
        if (token != null && !token.isEmpty() && !"null".equals(token)) {
            try {
                currentUserId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {}
        }

        final Long userId = currentUserId;

        return comments.stream().map(comment -> {
            WorkCommentVo vo = new WorkCommentVo();
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

            // 检查点赞状态
            if (userId != null) {
                ForumUserActions action = userActionsMapper.selectOne(Wrappers.lambdaQuery(ForumUserActions.class)
                        .eq(ForumUserActions::getUserId, userId)
                        .eq(ForumUserActions::getTargetId, comment.getId())
                        .eq(ForumUserActions::getTargetType, TargetType.LIKE_COMMENT) // 也可以单独加一个 LIKE_WORK_COMMENT
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
    public Boolean toggleCommentLike(String token, Long commentId) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "请先登录");
        }

        ForumUserActions action = userActionsMapper.selectOne(Wrappers.lambdaQuery(ForumUserActions.class)
                .eq(ForumUserActions::getUserId, userId)
                .eq(ForumUserActions::getTargetId, commentId)
                .eq(ForumUserActions::getTargetType, TargetType.LIKE_COMMENT));

        if (action != null) {
            int newStatus = action.getIsActive() == 1 ? 0 : 1;
            action.setIsActive(newStatus);
            userActionsMapper.updateById(action);
            workCommentsMapper.updateLikeCount(commentId, newStatus == 1 ? 1 : -1);
            return newStatus == 1;
        } else {
            action = new ForumUserActions();
            action.setUserId(userId);
            action.setTargetId(commentId);
            action.setTargetType(TargetType.LIKE_COMMENT);
            action.setIsActive(1);
            userActionsMapper.insert(action);
            workCommentsMapper.updateLikeCount(commentId, 1);
            return true;
        }
    }
}
