package com.is.inspirationspaceclient.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.forum.mapper.ForumPostStatsMapper;
import com.is.inspirationspaceclient.forum.mapper.ForumPostsMapper;
import com.is.inspirationspaceclient.forum.mapper.ForumUserActionsMapper;
import com.is.inspirationspaceclient.forum.model.dto.ForumCreateDto;
import com.is.inspirationspaceclient.forum.model.entity.ForumPostStats;
import com.is.inspirationspaceclient.forum.model.entity.ForumPosts;
import com.is.inspirationspaceclient.forum.model.entity.ForumUserActions;
import com.is.inspirationspaceclient.forum.model.entity.enums.TargetType;
import com.is.inspirationspaceclient.forum.model.vo.PostDetailVo;
import com.is.inspirationspaceclient.forum.model.vo.PostSimpleVo;

import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.exception.IsServiceException;
import com.is.inspirationspacecommon.redis.RedisCache;
import com.is.inspirationspacecommon.redis.RedisKeyBuild;
import com.is.inspirationspacecommon.redis.RedisKeyManage;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class ForumServiceImpl implements ForumService {


    private SnowflakeIdGenerator snowflakeIdGenerator;

    private ForumPostsMapper forumPostsMapper;

    private ForumPostStatsMapper forumPostStatsMapper;

    private ForumUserActionsMapper forumUserActionsMapper;

    private RedisCache redisCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createPost(String token, ForumCreateDto forumCreateDto) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }
        // 1. 插入帖子
        ForumPosts post = new ForumPosts();
        BeanUtils.copyProperties(forumCreateDto, post);
        post.setPostId(snowflakeIdGenerator.nextId());
        post.setUserId(userId);
        // 2. 插入帖子统计数据
        ForumPostStats stats = new ForumPostStats();
        stats.setPostId(post.getPostId());
        if(forumPostStatsMapper.insert(stats)>0&&forumPostsMapper.insert(post)>0){
            return true;
        }else {
            log.error("创建帖子失败, userId: {}, title: {}", userId, forumCreateDto.getTitle());
            throw new IsServiceException(ErrorCode.SYSTEM_ERROR.getHttpStatusCode(), "创建帖子失败");
        }

    }

    @Override
    public Boolean deletePost(Long postId) {
        return null;
    }


    @Override
    public Page<PostSimpleVo> getAllPosts(String sort, int page, int size) {
        Page<PostSimpleVo> pageObj = new Page<>(page, size);
        String orderByColumn = getOrderByColumn(sort);
        return forumPostsMapper.selectPostsWithStats(pageObj, orderByColumn);
    }

    @Override
    public PostDetailVo getPostDetail(Long postId) {
        return null;
    }

    private String getOrderByColumn(String sort) {
        return switch (sort) {
            case "like" -> "fps.like_count";
            case "repost" -> "fps.repost_count";
            case "view" -> "fps.view_count";
            case "collect" -> "fps.comment_count";
            default -> "fp.created_at";
        };
    }


    @Override
    public Boolean like(String token, Long postId) {
        // 1. 校验token
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 尝试插入点赞行为
        ForumUserActions action = new ForumUserActions();
        action.setUserId(userId);
        action.setTargetId(postId);
        action.setTargetType(TargetType.LIKE);

        try {
            forumUserActionsMapper.insert(action);
        } catch (DuplicateKeyException e) {
            return true; // 已点赞
        }

        // 3. 原子化更新缓存
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.FORUM_POST_STATS, postId);
        Long newCount = redisCache.incrHash(key, "like_count", 1);

        // 缓存缺失修复：首次操作时需要初始化
        if (newCount == 1) {
            ForumPostStats stats = forumPostStatsMapper.selectById(postId);
            long correctedCount = stats.getLikeCount() + 1; // 数据库值 + 本次操作
            redisCache.putHash(key, "like_count", correctedCount, 60 * 60 * 24 * 7, TimeUnit.SECONDS);
        }

        // 4. 异步更新数据库
        updatePostStatsAsync(postId, "like_count", 1);
        return true;
    }

    @Override
    public Boolean unlike(String token, Long postId) {
        // 1. 校验token
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 尝试删除点赞行为
        int deleted = forumUserActionsMapper.deleteByUserTarget(
                userId,
                postId,
                TargetType.LIKE
        );
        if (deleted == 0) {
            return false; // 未点赞
        }

        // 3. 原子化更新缓存
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.FORUM_POST_STATS, postId);
        Long newCount = redisCache.incrHash(key, "like_count", -1);

        // 缓存异常修复：防止负值
        if (newCount < 0) {
            ForumPostStats stats = forumPostStatsMapper.selectById(postId);
            long correctedCount = Math.max(0, stats.getLikeCount() - 1); // 数据库值 - 本次操作
            redisCache.putHash(key, "like_count", correctedCount, 60 * 60 * 24 * 7, TimeUnit.SECONDS);
        }

        // 4. 异步更新数据库
        updatePostStatsAsync(postId, "like_count", -1);
        return true;
    }

    @Override
    public Boolean repost(String token, Long postId) {
        // 1. 校验token
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 尝试插入转发行为
        ForumUserActions action = new ForumUserActions();
        action.setUserId(userId);
        action.setTargetId(postId);
        action.setTargetType(TargetType.REPOST);

        try {
            forumUserActionsMapper.insert(action);
        } catch (DuplicateKeyException e) {
            return true; // 已转发
        }

        // 3. 原子化更新缓存
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.FORUM_POST_STATS, postId);
        Long newCount = redisCache.incrHash(key, "repost_count", 1);

        if (newCount == 1) {
            ForumPostStats stats = forumPostStatsMapper.selectById(postId);
            redisCache.putHash(key, "repost_count", stats.getRepostCount() + 1, 60 * 60 * 24 * 7, TimeUnit.SECONDS);
        }

        // 4. 异步更新数据库
        updatePostStatsAsync(postId, "repost_count", 1);
        return true;
    }

    @Override
    public Boolean collect(String token, Long postId) {
        // 1. 校验token
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 尝试插入收藏行为
        ForumUserActions action = new ForumUserActions();
        action.setUserId(userId);
        action.setTargetId(postId);
        action.setTargetType(TargetType.COLLECT);

        try {
            forumUserActionsMapper.insert(action);
        } catch (DuplicateKeyException e) {
            return true; // 已收藏
        }

        // 3. 原子化更新缓存
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.FORUM_POST_STATS, postId);
        Long newCount = redisCache.incrHash(key, "collect_count", 1);

        if (newCount == 1) {
            ForumPostStats stats = forumPostStatsMapper.selectById(postId);
            redisCache.putHash(key, "collect_count", stats.getCollectCount() + 1, 60 * 60 * 24 * 7, TimeUnit.SECONDS);
        }

        // 4. 异步更新数据库
        updatePostStatsAsync(postId, "collect_count", 1);
        return true;
    }

    @Override
    public Boolean uncollect(String token, Long postId) {
        // 1. 校验token
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 尝试删除收藏行为
        int deleted = forumUserActionsMapper.deleteByUserTarget(
                userId,
                postId,
                TargetType.COLLECT
        );
        if (deleted == 0) {
            return false; // 未收藏
        }

        // 3. 原子化更新缓存
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.FORUM_POST_STATS, postId);
        Long newCount = redisCache.incrHash(key, "collect_count", -1);

        if (newCount < 0) {
            ForumPostStats stats = forumPostStatsMapper.selectById(postId);
            long correctedCount = Math.max(0, stats.getCollectCount() - 1);
            redisCache.putHash(key, "collect_count", correctedCount, 60 * 60 * 24 * 7, TimeUnit.SECONDS);
        }

        // 4. 异步更新数据库
        updatePostStatsAsync(postId, "collect_count", -1);
        return true;
    }

    /**
     * 异步更新帖子统计（核心方法）
     *
     * @param postId 帖子ID
     * @param field  统计字段（like_count/repost_count/collect_count）
     * @param delta  增量（+1或-1）
     */
    @Async // 关键注解：标记为异步方法
    public void updatePostStatsAsync(Long postId, String field, int delta) {
        try {
            // 执行原子更新
            int affectedRows = performUpdate(postId, field, delta);
            // 记录关键日志
            if (log.isDebugEnabled()) {
                log.debug("异步更新统计成功: postId={}, field={}, delta={}, rows={}",
                        postId, field, delta, affectedRows);
            }
        } catch (Exception e) {
            // 必须记录完整错误信息
            log.error("异步更新统计失败: postId={}, field={}, delta={}",
                    postId, field, delta, e);

            // 可选：实现简单重试（最多2次）
            try {
                Thread.sleep(100); // 短暂等待后重试
                performUpdate(postId, field, delta);
                log.info("重试成功: postId={}, field={}, delta={}", postId, field, delta);
            } catch (Exception ex) {
                log.error("最终重试失败: postId={}, field={}, delta={}",
                        postId, field, delta, ex);
            }
        }
    }

    /**
     * 执行实际的数据库更新（原子操作）
     */
    private int performUpdate(Long postId, String field, int delta) {
        return switch (field) {
            case "like_count" -> forumPostStatsMapper.updateLikeCount(postId, delta);
            case "repost_count" -> forumPostStatsMapper.updateRepostCount(postId, delta);
            case "collect_count" -> forumPostStatsMapper.updateCollectCount(postId, delta);
            default -> throw new IllegalArgumentException("无效的统计字段: " + field);
        };
    }

}
