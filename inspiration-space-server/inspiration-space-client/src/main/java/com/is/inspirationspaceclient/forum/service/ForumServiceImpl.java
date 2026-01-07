package com.is.inspirationspaceclient.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.forum.mapper.ForumCommentsMapper;
import com.is.inspirationspaceclient.forum.mapper.ForumPostStatsMapper;
import com.is.inspirationspaceclient.forum.mapper.ForumPostsMapper;
import com.is.inspirationspaceclient.forum.mapper.ForumUserActionsMapper;
import com.is.inspirationspaceclient.forum.model.dto.ForumCreateDto;
import com.is.inspirationspaceclient.forum.model.entity.ForumComments;
import com.is.inspirationspaceclient.forum.model.entity.ForumPostStats;
import com.is.inspirationspaceclient.forum.model.entity.ForumPosts;
import com.is.inspirationspaceclient.forum.model.entity.ForumUserActions;
import com.is.inspirationspaceclient.forum.model.entity.enums.TargetType;
import com.is.inspirationspaceclient.forum.model.vo.PostDetailVo;
import com.is.inspirationspaceclient.forum.model.vo.PostSimpleVo;

import com.is.inspirationspacecommon.config.StorageService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class ForumServiceImpl implements ForumService {

    private ForumCommentsMapper forumCommentsMapper;
    
    private ForumPostsMapper forumPostsMapper;

    private ForumPostStatsMapper forumPostStatsMapper;

    private ForumUserActionsMapper forumUserActionsMapper;

    private SnowflakeIdGenerator snowflakeIdGenerator;

    private RedisCache redisCache;

    private StorageService storageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createPost(String token, ForumCreateDto forumCreateDto) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 1. 生成帖子ID
        Long postId = snowflakeIdGenerator.nextId();

        // 2. 处理图片重命名 (从临时Key重命名为包含postId的Key)
        List<String> finalImageKeys = new ArrayList<>();
        if (forumCreateDto.getImageUrls() != null && !forumCreateDto.getImageUrls().isEmpty()) {
            for (int i = 0; i < forumCreateDto.getImageUrls().size(); i++) {
                String tempKey = forumCreateDto.getImageUrls().get(i);
                // 仅处理未重命名的临时图片 (以 post-image_ 开头)
                if (tempKey != null && tempKey.startsWith("post-image_")) {
                    String extension = "jpg";
                    if (tempKey.contains(".")) {
                        extension = tempKey.substring(tempKey.lastIndexOf(".") + 1);
                    }
                    String newKey = String.format("post-image_%s_%d.%s", postId, i, extension);
                    try {
                        // 复制并删除原临时文件 (相当于移动/重命名)
                        storageService.copyObject("post-image", tempKey, "post-image", newKey);
                        storageService.delete("post-image", tempKey);
                        finalImageKeys.add(newKey);
                    } catch (Exception e) {
                        log.error("重命名帖子图片失败: old={}, new={}", tempKey, newKey, e);
                        throw new IsServiceException(ErrorCode.FILE_UPLOAD_FAILED.getHttpStatusCode(), "图片处理失败");
                    }
                } else {
                    finalImageKeys.add(tempKey);
                }
            }
        }

        // 3. 插入帖子
        ForumPosts post = new ForumPosts();
        BeanUtils.copyProperties(forumCreateDto, post);
        post.setPostId(postId);
        post.setUserId(userId);
        post.setImageUrls(finalImageKeys); // 使用重命名后的Key列表

        // 4. 插入帖子统计数据
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePost(Long postId) {
        // 1. 检查帖子是否存在
        ForumPosts post = forumPostsMapper.selectById(postId);
        if (post == null) {
            throw new IsServiceException(ErrorCode.SYSTEM_ERROR.getHttpStatusCode(), "帖子不存在");
        }

        // 2. 删除帖子相关的评论
        forumCommentsMapper.delete(new LambdaQueryWrapper<ForumComments>()
                .eq(ForumComments::getPostId, postId));

        // 3. 删除帖子相关的用户行为记录（点赞、收藏、转发等）
        forumUserActionsMapper.delete(new LambdaQueryWrapper<ForumUserActions>()
                .eq(ForumUserActions::getTargetId, postId));

        // 4. 删除帖子统计信息
        forumPostStatsMapper.deleteById(postId);

        // 5. 删除帖子主记录（软删除）
        post.setIsDeleted(1);
        forumPostsMapper.updateById(post);

        // 6. 删除相关的图片文件（如果有）
        List<String> imageUrls = post.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageKey : imageUrls) {
                if (imageKey != null && !imageKey.startsWith("http")) {
                    try {
                        storageService.delete("post-image", imageKey);
                    } catch (Exception e) {
                        log.warn("删除帖子图片失败: bucket=post-image, key={}", imageKey, e);
                    }
                }
            }
        }

        return true;
    }


    @Override
    public Page<PostSimpleVo> getAllPosts(String token, String sort, String keyword, Integer category, int page, int size) {
        Page<PostSimpleVo> pageObj = new Page<>(page, size);
        String orderByColumn = getOrderByColumn(sort);

        Page<PostSimpleVo> postsPage = forumPostsMapper.selectPostsWithStats(pageObj, orderByColumn, keyword, category);

        // 1. 批量获取帖子实体以正确解析 JSON 格式的 imageUrls
        List<Long> postIds = postsPage.getRecords().stream()
                .map(PostSimpleVo::getPostId)
                .collect(Collectors.toList());
        
        Map<Long, List<String>> imageUrlMap = Map.of();
        if (!postIds.isEmpty()) {
            List<ForumPosts> posts = forumPostsMapper.selectList(new QueryWrapper<ForumPosts>().in("post_id", postIds));
            imageUrlMap = posts.stream()
                    .collect(Collectors.toMap(ForumPosts::getPostId, 
                            post -> post.getImageUrls() != null ? post.getImageUrls() : List.of()));
        }

        // 2. 处理图片URL和用户状态
        Long userId = null;
        if (token != null && !token.isEmpty() && !"null".equals(token)) {
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                log.warn("Invalid token in getAllPosts: {}", e.getMessage());
            }
        }

        for (PostSimpleVo vo : postsPage.getRecords()) {
            // 从 Map 中获取已正确解析的图片 Key 列表
            List<String> imageKeys = imageUrlMap.get(vo.getPostId());
            if (imageKeys != null && !imageKeys.isEmpty()) {
                vo.setImageUrls(convertKeysToUrls(imageKeys));
            }
            
            // 转换用户头像
            if (vo.getAvatar() != null && !vo.getAvatar().isEmpty() && !vo.getAvatar().startsWith("http")) {
                vo.setAvatar(getAvatarUrl(vo.getAvatar()));
            }

            if (userId != null) {
                vo.setIsLiked(checkAction(userId, vo.getPostId(), TargetType.LIKE));
                vo.setIsCollected(checkAction(userId, vo.getPostId(), TargetType.COLLECT));
                vo.setIsReposted(checkAction(userId, vo.getPostId(), TargetType.REPOST));
            }
        }

        return postsPage;
    }

    @Override
    public Page<PostSimpleVo> getCollectedPosts(String token, int page, int size) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        Page<PostSimpleVo> pageObj = new Page<>(page, size);
        Page<PostSimpleVo> postsPage = forumPostsMapper.selectCollectedPosts(pageObj, userId);

        // 批量获取帖子实体以正确解析 JSON 格式的 imageUrls
        List<Long> postIds = postsPage.getRecords().stream()
                .map(PostSimpleVo::getPostId)
                .collect(Collectors.toList());

        Map<Long, List<String>> imageUrlMap = Map.of();
        if (!postIds.isEmpty()) {
            List<ForumPosts> posts = forumPostsMapper.selectList(new QueryWrapper<ForumPosts>().in("post_id", postIds));
            imageUrlMap = posts.stream()
                    .collect(Collectors.toMap(ForumPosts::getPostId,
                            post -> post.getImageUrls() != null ? post.getImageUrls() : List.of()));
        }

        for (PostSimpleVo vo : postsPage.getRecords()) {
            List<String> imageKeys = imageUrlMap.get(vo.getPostId());
            if (imageKeys != null && !imageKeys.isEmpty()) {
                vo.setImageUrls(convertKeysToUrls(imageKeys));
            }

            if (vo.getAvatar() != null && !vo.getAvatar().isEmpty() && !vo.getAvatar().startsWith("http")) {
                vo.setAvatar(getAvatarUrl(vo.getAvatar()));
            }

            vo.setIsLiked(checkAction(userId, vo.getPostId(), TargetType.LIKE));
            vo.setIsCollected(true); // Since it's from the collected list
            vo.setIsReposted(checkAction(userId, vo.getPostId(), TargetType.REPOST));
        }

        return postsPage;
    }

    @Override
    public PostDetailVo getPostDetail(String token, Long postId) {
        PostDetailVo vo = forumPostsMapper.selectPostDetail(postId);
        if (vo == null) {
            throw new IsServiceException(ErrorCode.SYSTEM_ERROR.getHttpStatusCode(), "帖子不存在");
        }

        // 重新获取图片URL，确保JSON解析正确
        ForumPosts post = forumPostsMapper.selectById(postId);
        List<String> imageKeys = (post != null) ? post.getImageUrls() : null;

        // 转换图片URL为预签名URL
        if (imageKeys != null && !imageKeys.isEmpty()) {
            vo.setImageUrls(convertKeysToUrls(imageKeys));
        }

        // 增加浏览量
        updatePostStatsAsync(postId, "view_count", 1);

        // 如果用户已登录，填充状态字段
        Long userId = null;
        if (token != null && !token.isEmpty() && !"null".equals(token)) {
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                log.warn("Invalid token in getPostDetail: {}", e.getMessage());
            }
        }

        if (userId != null) {
            vo.setIsLiked(checkAction(userId, postId, TargetType.LIKE));
            vo.setIsCollected(checkAction(userId, postId, TargetType.COLLECT));
            vo.setIsReposted(checkAction(userId, postId, TargetType.REPOST));
        } else {
            vo.setIsLiked(false);
            vo.setIsCollected(false);
            vo.setIsReposted(false);
        }

        return vo;
    }

    @Override
    public String uploadImage(String token, org.springframework.web.multipart.MultipartFile file) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        try {
            // 1. 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                throw new IsArgumentException(ErrorCode.INVALID_FILE_TYPE.getHttpStatusCode(), "文件类型错误");
            }

            // 2. 验证文件大小（限制为5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IsArgumentException(ErrorCode.FILE_TOO_LARGE.getHttpStatusCode(), "文件过大");
            }

            // 3. 生成对象键 post-image_{userId}_时间戳.ext (因为此时还没有postId，暂用userId)
            String originalFilename = file.getOriginalFilename();
            String extension = "jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }
            String timestamp = String.valueOf(System.currentTimeMillis());
            String objectKey = String.format("post-image_%s_%s.%s", userId, timestamp, extension);

            // 4. 上传到 Minio 的 post-image 桶
            return storageService.upload(file, "post-image", objectKey);
        } catch (Exception e) {
            log.error("论坛图片上传失败", e);
            throw new IsServiceException(ErrorCode.FILE_UPLOAD_FAILED.getHttpStatusCode(), "图片上传失败");
        }
    }

    private List<String> convertKeysToUrls(List<String> keys) {
        if (keys == null) return null;
        return keys.stream().map(key -> {
            if (key == null || key.isEmpty() || key.startsWith("http")) return key;
            try {
                return storageService.getPreSignedUrl("post-image", key, 3, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("生成图片预签名URL失败: {}", key);
                return "";
            }
        }).filter(url -> !url.isEmpty()).toList();
    }

    private String getAvatarUrl(String key) {
        if (key == null || key.isEmpty() || key.startsWith("http")) return key;
        try {
            return storageService.getPreSignedUrl("avatars", key, 3, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("生成头像预签名URL失败: {}", key);
            return "";
        }
    }

    private boolean checkAction(Long userId, Long targetId, TargetType type) {
        return forumUserActionsMapper.selectCount(new LambdaQueryWrapper<ForumUserActions>()
                .eq(ForumUserActions::getUserId, userId)
                .eq(ForumUserActions::getTargetId, targetId)
                .eq(ForumUserActions::getTargetType, type)
                .eq(ForumUserActions::getIsActive, 1)) > 0;
    }

    private String getOrderByColumn(String sort) {
        return switch (sort) {
            case "like" -> "fps.like_count";
            case "repost" -> "fps.repost_count";
            case "view" -> "fps.view_count";
            case "comment" -> "fps.comment_count";
            case "collect" -> "fps.collect_count";
            default -> "fp.created_at";
        };
    }


    @Override
    public Boolean like(String token, Long postId) {
        return toggleAction(token, postId, TargetType.LIKE, 1);
    }

    @Override
    public Boolean unlike(String token, Long postId) {
        return toggleAction(token, postId, TargetType.LIKE, 0);
    }

    @Override
    public Boolean repost(String token, Long postId) {
        return toggleAction(token, postId, TargetType.REPOST, 1);
    }

    @Override
    public Boolean collect(String token, Long postId) {
        return toggleAction(token, postId, TargetType.COLLECT, 1);
    }

    @Override
    public Boolean uncollect(String token, Long postId) {
        return toggleAction(token, postId, TargetType.COLLECT, 0);
    }

    private Boolean toggleAction(String token, Long postId, TargetType type, int targetStatus) {
        // 1. 校验token
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (!JwtUtil.validateToken(token) || userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 检查记录是否存在
        ForumUserActions action = forumUserActionsMapper.selectOne(new LambdaQueryWrapper<ForumUserActions>()
                .eq(ForumUserActions::getUserId, userId)
                .eq(ForumUserActions::getTargetId, postId)
                .eq(ForumUserActions::getTargetType, type));

        if (action != null) {
            if (action.getIsActive() == targetStatus) {
                return true; // 状态已经符合要求
            }
            // 更改状态
            action.setIsActive(targetStatus);
            forumUserActionsMapper.updateById(action);
        } else {
            // 只有当目标状态为 1 时才创建记录
            if (targetStatus == 1) {
                action = new ForumUserActions();
                action.setUserId(userId);
                action.setTargetId(postId);
                action.setTargetType(type);
                action.setIsActive(1);
                forumUserActionsMapper.insert(action);
            } else {
                return false; // 本来就没有记录，无法执行取消操作
            }
        }

        // 3. 更新统计数据
        String field = switch (type) {
            case LIKE -> "like_count";
            case REPOST -> "repost_count";
            case COLLECT -> "collect_count";
            default -> throw new IsArgumentException("Unsupported action type");
        };

        int delta = (targetStatus == 1) ? 1 : -1;
        
        // 原子化更新缓存
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.FORUM_POST_STATS, postId);
        Long newCount = redisCache.incrHash(key, field, delta);

        // 缓存异常处理
        if (targetStatus == 1 && newCount == 1) {
            ForumPostStats stats = forumPostStatsMapper.selectById(postId);
            long val = 0;
            if (field.equals("like_count")) val = stats.getLikeCount();
            else if (field.equals("repost_count")) val = stats.getRepostCount();
            else if (field.equals("collect_count")) val = stats.getCollectCount();
            redisCache.putHash(key, field, val + 1, 60 * 60 * 24 * 7, TimeUnit.SECONDS);
        } else if (targetStatus == 0 && newCount < 0) {
            redisCache.putHash(key, field, 0L, 60 * 60 * 24 * 7, TimeUnit.SECONDS);
        }

        // 4. 异步更新数据库
        updatePostStatsAsync(postId, field, delta);
        return true;
    }

    /**
     * 异步更新帖子统计（核心方法）
     *
     * @param postId 帖子ID
     * @param field  统计字段（like_count/repost_count/collect_count）
     * @param delta  增量（+1或-1）
     */
    @Async 
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
            case "view_count" -> forumPostStatsMapper.updateViewCount(postId, delta);
            case "comment_count" -> forumPostStatsMapper.updateCommentCount(postId, delta);
            default -> throw new IllegalArgumentException("无效的统计字段: " + field);
        };
    }

}