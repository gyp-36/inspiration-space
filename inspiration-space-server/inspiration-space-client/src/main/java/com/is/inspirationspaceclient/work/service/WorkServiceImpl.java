package com.is.inspirationspaceclient.work.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.is.inspirationspaceclient.work.mapper.WorkAttachmentsMapper;
import com.is.inspirationspaceclient.work.mapper.WorkStatsMapper;
import com.is.inspirationspaceclient.work.model.dto.WorkCreateDto;
import com.is.inspirationspaceclient.work.model.dto.WorkUpdateDto;
import com.is.inspirationspaceclient.work.model.entity.WorkAttachment;
import com.is.inspirationspaceclient.work.model.entity.WorkInfo;
import com.is.inspirationspaceclient.work.model.entity.WorkStats;
import com.is.inspirationspaceclient.work.mapper.TagMapper;
import com.is.inspirationspaceclient.work.mapper.WorkTagsMapper;
import com.is.inspirationspaceclient.work.model.entity.Tag;
import com.is.inspirationspaceclient.work.model.entity.WorkTag;
import com.is.inspirationspaceclient.work.model.entity.enums.Status;
import com.is.inspirationspaceclient.work.model.entity.enums.Visibility;
import com.is.inspirationspaceclient.work.model.vo.WorkDetailVo;
import com.is.inspirationspaceclient.work.model.vo.WorkSimpleVo;
import com.is.inspirationspaceclient.work.mapper.WorkInfoMapper;
import com.is.inspirationspaceclient.user.mapper.UserMapper;
import com.is.inspirationspaceclient.user.model.entity.User;
import com.is.inspirationspaceclient.forum.mapper.ForumUserActionsMapper;
import com.is.inspirationspaceclient.forum.model.entity.ForumUserActions;
import com.is.inspirationspaceclient.forum.model.entity.enums.TargetType;
import com.is.inspirationspacecommon.config.StorageService;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.exception.IsServiceException;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 作品表 服务实现类
 * </p>
 *
 * @author
 * @since 2025-07-07
 */
@Service
@Slf4j
public class WorkServiceImpl implements WorkService {

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private WorkInfoMapper workInfoMapper;
    @Autowired
    private WorkAttachmentsMapper workAttachmentsMapper;

    @Autowired
    private StorageService storageService;
    @Autowired
    private WorkStatsMapper workStatsMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private WorkTagsMapper workTagsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ForumUserActionsMapper userActionsMapper;

    @Autowired
    private WorkStateService workStateService;

    /**
     * 创建作品草稿
     *
     * @param token 前端传入的Authorization头
     * @param workCreateDto 作品创建DTO
     * @return 新创建的作品ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(String token, WorkCreateDto workCreateDto) {
        //1.解析token,获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        //2.创建作品副本（填信息）
        WorkInfo workInfo = new WorkInfo();
        workInfo.setWorkId(snowflakeIdGenerator.nextId());
        workInfo.setCreatorId(userId);
        BeanUtils.copyProperties(workCreateDto, workInfo);
        workInfo.setStatus(Status.WRITING);

        // 3. 处理封面图
        if (StringUtils.isNotBlank(workCreateDto.getCoverUrl())) {
            // 尝试从URL中提取Key
            String coverKey = extractKeyFromUrl(workCreateDto.getCoverUrl());
            // URL解码
            if (coverKey != null) {
                try {
                    coverKey = java.net.URLDecoder.decode(coverKey, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e) {
                   log.warn("封面Key解码失败", e);
                }
            }
            workInfo.setCoverUrl(coverKey);
        }

        // 4. 保存作品
        workInfoMapper.insert(workInfo);

        // 5. 处理附件关联关系
        handleAttachmentRelations(workInfo.getWorkId(), workCreateDto.getAttachmentIds());

        // 6. 处理标签关联关系
        handleTagRelations(workInfo.getWorkId(), workCreateDto.getTags());

        return workInfo.getWorkId();
    }

    /**
     * 更新草稿
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDraft(Long workId, String token, WorkCreateDto workCreateDto) {
        // 1. 验证token并获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 获取当前草稿并验证权限
        WorkInfo workInfo = workInfoMapper.selectById(workId);
        if (workInfo == null) {
            throw new IsArgumentException(ErrorCode.WORK_NOT_FOUND.getHttpStatusCode(), "作品不存在");
        }
        if (!workInfo.getCreatorId().equals(userId)) {
            throw new IsArgumentException(ErrorCode.PERMISSION_DENIED.getHttpStatusCode(), "无权修改非本人作品");
        }
        
        // 3. 处理封面图更新
        if (StringUtils.isNotBlank(workCreateDto.getCoverUrl())) {
            // 尝试从URL中提取Key
            String coverKey = extractKeyFromUrl(workCreateDto.getCoverUrl());
            // URL解码
            if (coverKey != null) {
                try {
                    coverKey = java.net.URLDecoder.decode(coverKey, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("封面Key解码失败", e);
                }
            }
            workInfo.setCoverUrl(coverKey);
        }

        // 4. 处理附件关联关系
        handleAttachmentRelations(workId, workCreateDto.getAttachmentIds());

        // 5. 处理标签关联关系
        handleTagRelations(workId, workCreateDto.getTags());

        // 6. 更新作品基础信息（排除系统字段）
        BeanUtils.copyProperties(workCreateDto, workInfo,
                "id", "creator_id", "status", "created_at", "published_at", "deleted_at",
                "total_revenue", "total_sales", "updated_at");

        // 7. 更新时间
        workInfo.setUpdatedAt(LocalDateTime.now());

        // 8. 保存更新
        workInfoMapper.updateById(workInfo);

        return true;
    }

    /**
     * 处理附件关联关系更新
     *
     * @param workId           当前作品ID
     * @param newAttachmentIds 前端传入的新附件ID列表
     */
    private void handleAttachmentRelations(Long workId, List<Long> newAttachmentIds) {
        if (newAttachmentIds == null) {
            return;
        }

        // 1. 获取当前作品已关联的附件
        List<WorkAttachment> currentAttachments = workAttachmentsMapper.selectByWorkId(workId);
        Set<Long> currentAttachmentIds = currentAttachments.stream()
                .map(WorkAttachment::getId)
                .collect(Collectors.toSet());

        // 2. 处理新增附件
        for (Long attachmentId : newAttachmentIds) {
            // 检查附件是否存在
            WorkAttachment attachment = workAttachmentsMapper.selectById(attachmentId);
            if (attachment == null) {
                continue;
            }

            // 关联到当前作品
            if (attachment.getWorkId() == null || !attachment.getWorkId().equals(workId)) {
                attachment.setWorkId(workId);
                workAttachmentsMapper.updateById(attachment);
            }
        }

        // 3. 处理移除附件
        List<Long> removeAttachmentIds = currentAttachmentIds.stream()
                .filter(id -> !newAttachmentIds.contains(id))
                .collect(Collectors.toList());

        for (Long attachmentId : removeAttachmentIds) {
            WorkAttachment attachment = workAttachmentsMapper.selectById(attachmentId);
            if (attachment != null) {
                // 解除与当前作品的关联（保留附件数据）
                attachment.setWorkId(null);
                workAttachmentsMapper.updateById(attachment);
            }
        }
    }

    /**
     * 处理标签关联关系更新
     *
     * @param workId   当前作品ID
     * @param tagNames 前端传入的标签名称列表
     */
    private void handleTagRelations(Long workId, List<String> tagNames) {
        if (tagNames == null) {
            return;
        }

        // 1. 删除旧的关联
        workTagsMapper.delete(new QueryWrapper<WorkTag>().eq("work_id", workId));

        // 2. 如果没有新标签，直接返回
        if (tagNames.isEmpty()) {
            return;
        }

        // 3. 处理每一个标签名称
        for (String tagName : tagNames) {
            if (StringUtils.isBlank(tagName)) continue;

            // 3.1 查找或创建标签
            Tag tag = tagMapper.selectOne(new QueryWrapper<Tag>().eq("tag_name", tagName));
            if (tag == null) {
                tag = new Tag();
                tag.setTagName(tagName);
                tagMapper.insert(tag);
            }

            // 3.2 创建关联
            WorkTag workTag = new WorkTag();
            workTag.setWorkId(workId);
            workTag.setTagId(tag.getTagId());
            workTagsMapper.insert(workTag);
        }
    }


    /**
     * 发布作品
     *
     * @param token
     * @param workId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishWork(String token, Long workId) {
        //1.解析token,获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }
        //2.获取作品,判断是否是作者
        WorkInfo workInfo = workInfoMapper.selectById(workId);
        if (workInfo == null) {
            throw new IsArgumentException(ErrorCode.WORK_NOT_FOUND.getHttpStatusCode(), "作品不存在");
        }
        if (!workInfo.getCreatorId().equals(userId)) {
            throw new IsArgumentException(ErrorCode.PERMISSION_DENIED.getHttpStatusCode(), "无权限");
        }
        
        //3.发布作品
        if (workInfo.getStatus() != Status.PUBLISHED) {
            //修改状态
            workInfo.setStatus(Status.PUBLISHED);
            workInfo.setPublishedAt(LocalDateTime.now());
            workInfoMapper.updateById(workInfo);

            // 同步更新关联的附件状态
            List<WorkAttachment> attachments = workAttachmentsMapper.selectByWorkId(workId);
            for (WorkAttachment attachment : attachments) {
                // 默认发布后附件也应随作品状态可用
                attachment.setUpdatedAt(LocalDateTime.now());
                workAttachmentsMapper.updateById(attachment);
            }

            // 4. 创建统计表 (如果不存在)
            WorkStats existingStats = workStatsMapper.selectOne(new QueryWrapper<WorkStats>().eq("work_id", workId));
            if (existingStats == null) {
                WorkStats workStats = new WorkStats();
                workStats.setWorkId(workId);
                workStatsMapper.insert(workStats);
            }
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWork(Long workId, String token) {
        //1.解析token,获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }
        //2.获取作品,判断是否是作者
        WorkInfo workInfo = workInfoMapper.selectById(workId);
        if (workInfo == null) {
            throw new IsArgumentException(ErrorCode.WORK_NOT_FOUND.getHttpStatusCode(), "作品不存在");
        }
        if (!workInfo.getCreatorId().equals(userId)) {
            throw new IsArgumentException(ErrorCode.PERMISSION_DENIED.getHttpStatusCode(), "无权删除非本人作品");
        }
        if (workInfo.getDeletedAt() != null) {
            throw new IsArgumentException(ErrorCode.WORK_ALREADY_DELETED.getHttpStatusCode(), "作品已被删除");
        }
        //3.删除作品(进入垃圾箱，设置过期时间，过期自动删除)
        workInfo.setDeletedAt(LocalDateTime.now());
        workInfoMapper.updateById(workInfo);
        //4.异步发送消息
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWork(Long workId, String token, WorkUpdateDto workUpdateDto) {
        // 1. 验证token并获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 获取作品并验证权限
        WorkInfo workInfo = workInfoMapper.selectById(workId);
        if (workInfo == null) {
            throw new IsArgumentException(ErrorCode.WORK_NOT_FOUND.getHttpStatusCode(), "作品不存在");
        }
        if (!workInfo.getCreatorId().equals(userId)) {
            throw new IsArgumentException(ErrorCode.PERMISSION_DENIED.getHttpStatusCode(), "无权修改非本人作品");
        }
        if (workInfo.getDeletedAt() != null) {
            throw new IsArgumentException(ErrorCode.WORK_ALREADY_DELETED.getHttpStatusCode(), "作品已被删除");
        }

        // 3. 状态校验：仅允许修改草稿/审核拒绝状态
        if (workInfo.getStatus() != Status.WRITING && workInfo.getStatus() != Status.REJECTED) {
            throw new IsArgumentException(ErrorCode.INVALID_STATUS.getHttpStatusCode(),
                    "仅允许修改草稿(0)或审核拒绝(4)状态的作品");
        }

        // 4. 处理附件关联关系
        handleAttachmentRelations(workId, workUpdateDto.getAttachmentIds());

        // 5. 处理标签关联关系
        handleTagRelations(workId, workUpdateDto.getTags());

        // 6. 更新字段（排除系统字段）
        BeanUtils.copyProperties(workUpdateDto, workInfo,
                "id", "creator_id", "status", "created_at", "published_at", "deleted_at",
                "total_revenue", "total_sales", "updated_at", "coverUrl");

        // 特别处理封面图
        if (StringUtils.isNotBlank(workUpdateDto.getCoverUrl())) {
            String coverKey = extractKeyFromUrl(workUpdateDto.getCoverUrl());
            if (coverKey != null) {
                try {
                    coverKey = java.net.URLDecoder.decode(coverKey, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("封面Key解码失败", e);
                }
            }
            workInfo.setCoverUrl(coverKey);
        }

        // 7. 更新时间戳
        workInfo.setUpdatedAt(LocalDateTime.now());

        // 8. 保存更新
        workInfoMapper.updateById(workInfo);

        // 7. 异步发送消息
//        messageService.sendWorkUpdatedMessage(workInfo);


        return true;
    }


    /**
     * 获取作品详情(个人)
     *
     * @param token
     * @param workId
     * @return
     */
    @Override
    public WorkDetailVo getWorkDetail(String token, Long workId) {
        //1.解析token,获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }
        //2.判断是否是作者，获取作品
        WorkInfo workInfo = workInfoMapper.selectById(workId);
        if (!workInfo.getCreatorId().equals(userId)) {
            throw new IsArgumentException(ErrorCode.PERMISSION_DENIED.getHttpStatusCode(), "无权获取非本人作品详情");
        }
        if (workInfo.getDeletedAt() != null) {
            throw new IsArgumentException(ErrorCode.WORK_ALREADY_DELETED.getHttpStatusCode(), "作品已被删除");
        }
        WorkDetailVo workDetailVo = new WorkDetailVo();
        BeanUtils.copyProperties(workInfo, workDetailVo, "create_at");
        // 处理封面图
        String coverKey = workInfo.getCoverUrl();
        if (StringUtils.isNotBlank(coverKey)) {
             // 尝试提取 ObjectKey (如果数据库存的是完整URL)
            String objectKey = coverKey;
            if (coverKey.startsWith("http") && coverKey.contains("/work/")) {
                try {
                    int bucketIndex = coverKey.indexOf("/work/");
                    String path = coverKey.substring(bucketIndex + "/work/".length());
                    int queryIndex = path.indexOf("?");
                    if (queryIndex != -1) {
                        objectKey = path.substring(0, queryIndex);
                    } else {
                        objectKey = path;
                    }
                    objectKey = java.net.URLDecoder.decode(objectKey, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("尝试从URL提取Key失败: {}", coverKey);
                }
            }

            if (!objectKey.startsWith("http")) {
                // 如果是对象键，生成预签名URL
                try {
                    String signedUrl = storageService.getPreSignedUrl("work", objectKey, 1, TimeUnit.HOURS);
                    workDetailVo.setCoverUrl(signedUrl);
                } catch (Exception e) {
                    log.error("生成作品封面预签名URL失败: {}", objectKey, e);
                    workDetailVo.setCoverUrl(null);
                }
            } else {
                workDetailVo.setCoverUrl(coverKey);
            }
        }
        //3. 获取作者信息
        User user = userMapper.selectById(workInfo.getCreatorId());
        if (user != null) {
            workDetailVo.setAuthorName(user.getUsername());
            String avatarKey = user.getAvatarUrl();
            if (StringUtils.isNotBlank(avatarKey)) {
                if (avatarKey.startsWith("http")) {
                    workDetailVo.setAuthorAvatar(avatarKey);
                } else {
                    try {
                        String signedUrl = storageService.getPreSignedUrl("avatars", avatarKey, 1, TimeUnit.HOURS);
                        workDetailVo.setAuthorAvatar(signedUrl);
                    } catch (Exception e) {
                        log.error("生成作者头像预签名URL失败: {}", avatarKey, e);
                        workDetailVo.setAuthorAvatar(null);
                    }
                }
            }
        }

        // 4. 获取统计数据
        WorkStats stats = workStatsMapper.selectOne(new QueryWrapper<WorkStats>().eq("work_id", workId));
        if (stats != null) {
            workDetailVo.setViewCount(stats.getViewCount());
            workDetailVo.setLikeCount(stats.getLikeCount());
            workDetailVo.setCollectCount(stats.getFavoriteCount());
            workDetailVo.setCommentCount(stats.getCommentCount());
            workDetailVo.setPurchaseCount(stats.getPurchaseCount());
        } else {
            workDetailVo.setViewCount(0);
            workDetailVo.setLikeCount(0);
            workDetailVo.setCollectCount(0);
            workDetailVo.setCommentCount(0);
            workDetailVo.setPurchaseCount(0);
        }

        // 5. 返回作品详情
        return workDetailVo;
    }

    /**
     * 获取公开作品详情
     */
    @Override
    public WorkDetailVo getPublicWorkDetail(Long workId, String token) {
        WorkInfo workInfo = workInfoMapper.selectById(workId);
        if (workInfo == null || workInfo.getStatus() != Status.PUBLISHED || workInfo.getDeletedAt() != null) {
            throw new IsArgumentException(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatusCode(), "作品不存在或未公开");
        }

        // 增加浏览量
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            userId = JwtUtil.getUserIdFromToken(token);
        }
        workStateService.incrementViewCount(workId, userId);

        WorkDetailVo workDetailVo = new WorkDetailVo();
        BeanUtils.copyProperties(workInfo, workDetailVo, "create_at");
        workDetailVo.setAuthorId(workInfo.getCreatorId());
        
        // 处理封面图逻辑 (复用)
        String coverKey = workInfo.getCoverUrl();
        if (StringUtils.isNotBlank(coverKey)) {
            // ... (复用之前的逻辑，简化处理)
            if (!coverKey.startsWith("http")) {
                 try {
                    String signedUrl = storageService.getPreSignedUrl("work", coverKey, 1, TimeUnit.HOURS);
                    workDetailVo.setCoverUrl(signedUrl);
                } catch (Exception e) {
                    workDetailVo.setCoverUrl(null);
                }
            } else {
                workDetailVo.setCoverUrl(coverKey);
            }
        }

        // 获取作者信息
        User user = userMapper.selectById(workInfo.getCreatorId());
        if (user != null) {
            workDetailVo.setAuthorName(user.getUsername());
            String avatarKey = user.getAvatarUrl();
            if (StringUtils.isNotBlank(avatarKey)) {
                if (avatarKey.startsWith("http")) {
                    workDetailVo.setAuthorAvatar(avatarKey);
                } else {
                    try {
                        String signedUrl = storageService.getPreSignedUrl("avatars", avatarKey, 1, TimeUnit.HOURS);
                        workDetailVo.setAuthorAvatar(signedUrl);
                    } catch (Exception e) {
                        workDetailVo.setAuthorAvatar(null);
                    }
                }
            }
        }

        // 获取统计数据
        WorkStats stats = workStateService.getWorkStats(workId);
        if (stats != null) {
            workDetailVo.setViewCount(stats.getViewCount());
            workDetailVo.setLikeCount(stats.getLikeCount());
            workDetailVo.setCollectCount(stats.getFavoriteCount());
            workDetailVo.setCommentCount(stats.getCommentCount());
            workDetailVo.setPurchaseCount(stats.getPurchaseCount());
        }

        // 检查当前用户交互状态
        if (StringUtils.isNotBlank(token)) {
            if (userId != null) {
                workDetailVo.setIsLiked(checkUserAction(userId, workId, TargetType.LIKE));
                workDetailVo.setIsCollected(checkUserAction(userId, workId, TargetType.COLLECT));
            }
        }

        return workDetailVo;
    }

    /**
     * 获取用户作品列表(所有人)
     *
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<WorkSimpleVo> getUserWorks(Long userId, int page, int size) {
        // 1. 获取当前登录用户ID（用于权限判断）
        String auth = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        Long currentUserId = JwtUtil.getUserIdFromToken(auth);

        // 2. 构建查询条件
        QueryWrapper<WorkInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("creator_id", userId)
                .isNull("deleted_at") // 未删除的作品
                .ne("status", Status.DELETED) // 非删除状态
                .orderByDesc("updated_at");

        // 3. 分页查询
        Page<WorkInfo> workPage = workInfoMapper.selectPage(
                new Page<>(page, size),
                queryWrapper
        );

        // 4. 构建VO列表
        List<WorkSimpleVo> vos = workPage.getRecords().stream()
                .map(work -> convertToSimpleVo(work, currentUserId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Page<WorkSimpleVo> resultPage = new Page<>(workPage.getCurrent(), workPage.getSize(), workPage.getTotal());
        resultPage.setRecords(vos);
        return resultPage;
    }

    @Override
    public Page<WorkSimpleVo> getPublicWorks(int page, int size, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        QueryWrapper<WorkInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", Status.PUBLISHED)
                .eq("visibility", Visibility.PUBLIC)
                .isNull("deleted_at")
                .orderByDesc("published_at");

        Page<WorkInfo> workPage = workInfoMapper.selectPage(new Page<>(page, size), queryWrapper);

        List<WorkSimpleVo> vos = workPage.getRecords().stream()
                .map(work -> convertToSimpleVo(work, currentUserId))
                .collect(Collectors.toList());

        Page<WorkSimpleVo> resultPage = new Page<>(workPage.getCurrent(), workPage.getSize(), workPage.getTotal());
        resultPage.setRecords(vos);
        return resultPage;
    }

    @Override
    public Page<WorkSimpleVo> searchWorks(String keyword, int page, int size, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        QueryWrapper<WorkInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", Status.PUBLISHED)
                .eq("visibility", Visibility.PUBLIC)
                .isNull("deleted_at")
                .and(qw -> qw.like("title", keyword).or().like("description", keyword))
                .orderByDesc("published_at");

        Page<WorkInfo> workPage = workInfoMapper.selectPage(new Page<>(page, size), queryWrapper);

        List<WorkSimpleVo> vos = workPage.getRecords().stream()
                .map(work -> convertToSimpleVo(work, currentUserId))
                .collect(Collectors.toList());

        Page<WorkSimpleVo> resultPage = new Page<>(workPage.getCurrent(), workPage.getSize(), workPage.getTotal());
        resultPage.setRecords(vos);
        return resultPage;
    }

    private WorkSimpleVo convertToSimpleVo(WorkInfo work, Long currentUserId) {
        // 权限检查
        if (work.getVisibility() != Visibility.PUBLIC) {
            if (currentUserId == null || !currentUserId.equals(work.getCreatorId())) {
                return null;
            }
        }

        WorkSimpleVo vo = new WorkSimpleVo();
        BeanUtils.copyProperties(work, vo);
        vo.setWorkId(work.getWorkId());
        vo.setAccessStrategy(work.getAccessStrategy() != null ? work.getAccessStrategy().name() : null);

        // 获取作者信息
        User user = userMapper.selectById(work.getCreatorId());
        if (user != null) {
            vo.setAuthorId(user.getUserId());
            vo.setAuthorName(user.getUsername());
            
            // 处理作者头像 URL
            String avatarKey = user.getAvatarUrl();
            if (StringUtils.isNotBlank(avatarKey)) {
                if (avatarKey.startsWith("http")) {
                    vo.setAuthorAvatar(avatarKey);
                } else {
                    try {
                        String signedUrl = storageService.getPreSignedUrl("avatars", avatarKey, 1, TimeUnit.HOURS);
                        vo.setAuthorAvatar(signedUrl);
                    } catch (Exception e) {
                        log.error("生成作者头像预签名URL失败: {}", avatarKey, e);
                        vo.setAuthorAvatar(null);
                    }
                }
            }
        }

        // 处理作品封面 URL
        String coverKey = work.getCoverUrl();
        if (StringUtils.isNotBlank(coverKey)) {
            // 尝试提取 ObjectKey (如果数据库存的是完整URL)
            String objectKey = coverKey;
            if (coverKey.startsWith("http") && coverKey.contains("/work/")) {
                try {
                    int bucketIndex = coverKey.indexOf("/work/");
                    String path = coverKey.substring(bucketIndex + "/work/".length());
                    int queryIndex = path.indexOf("?");
                    if (queryIndex != -1) {
                        objectKey = path.substring(0, queryIndex);
                    } else {
                        objectKey = path;
                    }
                    // URL解码 (处理空格等特殊字符)
                    objectKey = java.net.URLDecoder.decode(objectKey, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("尝试从URL提取Key失败: {}", coverKey);
                }
            }

            if (!objectKey.startsWith("http")) {
                // 如果是对象键，生成预签名URL
                try {
                    String signedUrl = storageService.getPreSignedUrl("work", objectKey, 1, TimeUnit.HOURS);
                    vo.setCoverUrl(signedUrl);
                } catch (Exception e) {
                    log.error("生成作品封面预签名URL失败: {}", objectKey, e);
                    vo.setCoverUrl(null);
                }
            } else {
                // 无法提取Key或本来就是外部URL，直接透传
                vo.setCoverUrl(coverKey);
            }
        }

        // 获取统计数据
        WorkStats stats = workStateService.getWorkStats(work.getWorkId());
        if (stats != null) {
            vo.setViewCount(stats.getViewCount());
            vo.setLikeCount(stats.getLikeCount());
            vo.setCollectCount(stats.getFavoriteCount());
            vo.setCommentCount(stats.getCommentCount());
            vo.setPurchaseCount(stats.getPurchaseCount());
        } else {
            vo.setViewCount(0);
            vo.setLikeCount(0);
            vo.setCollectCount(0);
            vo.setCommentCount(0);
            vo.setPurchaseCount(0);
        }

        // 填充用户是否点赞/收藏状态
        if (currentUserId != null) {
            vo.setIsLiked(checkUserAction(currentUserId, work.getWorkId(), TargetType.LIKE));
            vo.setIsCollected(checkUserAction(currentUserId, work.getWorkId(), TargetType.COLLECT));
        } else {
            vo.setIsLiked(false);
            vo.setIsCollected(false);
        }

        return vo;
    }

    private Boolean checkUserAction(Long userId, Long workId, TargetType type) {
        QueryWrapper<ForumUserActions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("target_type", type)
                .eq("target_id", workId)
                .eq("is_active", 1);
        return userActionsMapper.exists(queryWrapper);
    }

    /**
     * 上传封面图
     *
     * @param file
     * @param token
     * @return
     */
    @Override
    public String uploadCover(MultipartFile file, String token, Long workId) {
        // 1. 验证token权限
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) throw new IsArgumentException("无效token");

        // 2. 校验图片格式（仅允许常见图片类型）
        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) &&
                !"image/png".equals(contentType) &&
                !"image/gif".equals(contentType)) {
            throw new IsArgumentException("仅支持JPG/PNG/GIF格式封面图");
        }

        // 3. 校验图片大小（不超过 10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IsArgumentException("封面图大小不得超过 10MB");
        }

        // 4. 上传到MinIO（名为work的桶，work-cover文件夹）
        String bucketName = "work";
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String objectKey = "work-cover/work-cover_" + (workId != null ? workId : "unknown") + "_" + userId + "_" + System.currentTimeMillis() + "." + extension;
        storageService.upload(file, bucketName, objectKey);

        // 5. 返回预签名URL（有效期5分钟）
        // 注意：前端拿到这个URL后预览，保存草稿时会将此URL传回。
        // createDraft/updateDraft 必须负责从URL中提取Key进行存储，否则URL过期后无法访问。
        return storageService.getPreSignedUrl(bucketName, objectKey, 5, TimeUnit.MINUTES);
    }

    /**
     * 从MinIO预签名URL中提取ObjectKey
     */
    private String extractKeyFromUrl(String url) {
        if (StringUtils.isBlank(url)) return null;
        // 如果本身不包含http，假设它已经是Key
        if (!url.startsWith("http")) return url;
        
        try {
            // 假设URL结构包含 /work/ (bucket name)
            int bucketIndex = url.indexOf("/work/");
            if (bucketIndex != -1) {
                String path = url.substring(bucketIndex + "/work/".length());
                int queryIndex = path.indexOf("?");
                if (queryIndex != -1) {
                    return path.substring(0, queryIndex);
                }
                return path;
            }
        } catch (Exception e) {
            log.warn("解析封面URL失败: {}", url);
        }
        // 解析失败返回原值，由后续逻辑处理
        return url;
    }
}
