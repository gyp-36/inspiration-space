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
import com.is.inspirationspaceclient.work.model.entity.enums.Status;
import com.is.inspirationspaceclient.work.model.entity.enums.Visibility;
import com.is.inspirationspaceclient.work.model.vo.WorkDetailVo;
import com.is.inspirationspaceclient.work.model.vo.WorkSimpleVo;
import com.is.inspirationspaceclient.work.mapper.WorkInfoMapper;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

    /**
     * 创建草稿
     *
     * @param token
     * @param workCreateDto
     * @return
     */
    @Override
    public Boolean createDraft(String token, WorkCreateDto workCreateDto) {
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
            // 封面图URL已由前端上传时直接返回，无需额外校验
            workInfo.setCoverUrl(workCreateDto.getCoverUrl());
        } else {
            throw new IsArgumentException("封面图必填");
        }

        // 4. 保存作品
        workInfoMapper.insert(workInfo);


        // 5. 处理普通附件（关联到 work_attachment 表）
        if (CollectionUtils.isNotEmpty(workCreateDto.getAttachmentIds())) {
            for (Long attachmentId : workCreateDto.getAttachmentIds()) {
                WorkAttachment attachment = workAttachmentsMapper.selectById(attachmentId);
                if (attachment == null) {
                    throw new IsServiceException("附件不存在");
                }
                // 关联作品ID
                attachment.setWorkId(workInfo.getWorkId());
                workAttachmentsMapper.insert(attachment);
            }
        }

        // 5. 异步发送消息
//        messageService.sendDraftCreatedMessage(workInfo);
        return true;
    }

    /**
     * 发布作品
     *
     * @param token
     * @param workId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
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
        if (workInfo.getStatus() != Status.WRITING) {
            throw new IsArgumentException(ErrorCode.INVALID_STATUS.getHttpStatusCode(), "仅允许修改草稿状态作品");
        }

        // 3. 处理封面图更新
        if (StringUtils.isNotBlank(workCreateDto.getCoverUrl())) {
            // 校验封面图URL格式
            if (!workCreateDto.getCoverUrl().startsWith("https://") && !workCreateDto.getCoverUrl().startsWith("http://")) {
                throw new IsArgumentException("封面图URL格式无效");
            }
            workInfo.setCoverUrl(workCreateDto.getCoverUrl());
        }

        // 4. 处理附件关联关系
        handleAttachmentRelations(workId, workCreateDto.getAttachmentIds());

        // 5. 更新作品基础信息（排除自增/系统字段）
        BeanUtils.copyProperties(workCreateDto, workInfo,
                "id", "creator_id", "status", "created_at", "published_at", "deleted_at",
                "total_revenue", "total_sales", "updated_at");

        // 6. 更新更新时间（由数据库自动维护）
        workInfo.setUpdatedAt(LocalDateTime.now());

        // 7. 保存更新
        workInfoMapper.updateById(workInfo);


        // 9. 异步发送消息（如更新通知）
//        messageService.sendDraftUpdatedMessage(workInfo);

        return true;
    }

    /**
     * 处理附件关联关系更新
     *
     * @param workId           当前作品ID
     * @param newAttachmentIds 前端传入的新附件ID列表
     */
    private void handleAttachmentRelations(Long workId, List<Long> newAttachmentIds) {
        // 1. 获取当前作品已关联的附件
        List<WorkAttachment> currentAttachments = workAttachmentsMapper.selectByWorkId(workId);
        Set<Long> currentAttachmentIds = currentAttachments.stream()
                .map(WorkAttachment::getId)
                .collect(Collectors.toSet());

        // 2. 处理新增附件
        if (CollectionUtils.isNotEmpty(newAttachmentIds)) {
            for (Long attachmentId : newAttachmentIds) {
                // 检查附件是否存在
                WorkAttachment attachment = workAttachmentsMapper.selectById(attachmentId);
                if (attachment == null) {
                    throw new IsArgumentException("附件ID不存在: " + attachmentId);
                }

                // 检查附件是否已被其他作品占用
                if (attachment.getWorkId() != null && !attachment.getWorkId().equals(workId)) {
                    throw new IsArgumentException("附件已被其他作品使用: " + attachmentId);
                }

                // 关联到当前作品
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
     * 发布作品
     *
     * @param token
     * @param workId
     * @return
     */
    @Override
    public Boolean publishWork(String token, Long workId) {
        //1.解析token,获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }
        //2.获取作品,判断是否是作者
        WorkInfo workInfo = workInfoMapper.selectById(workId);
        if (!workInfo.getCreatorId().equals(userId)) {
            throw new IsArgumentException("无权限");
        }
        //3.发布作品
        if (workInfo.getStatus() != Status.WRITING) {
            //修改状态
            workInfo.setStatus(Status.PUBLISHED);
            workInfoMapper.updateById(workInfo);

            // 创建统计表
            WorkStats workStats = new WorkStats();
            workStats.setWorkId(workId);
            workStatsMapper.insert(workStats);
        }
        //4.异步发送消息
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

        // 4. 更新字段（排除系统字段）
        BeanUtils.copyProperties(workUpdateDto, workInfo,
                "id", "creator_id", "status", "created_at", "published_at", "deleted_at",
                "total_revenue", "total_sales", "updated_at");

        // 5. 更新时间戳
        workInfo.setUpdatedAt(LocalDateTime.now());

        // 6. 保存更新
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
        //3.返回作品详情

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
        Long currentUserId = JwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication().getCredentials().toString());

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

        // 4. 构建VO列表（根据当前用户权限过滤）
        List<WorkSimpleVo> vos = workPage.getRecords().stream().map(work -> {
            WorkSimpleVo vo = new WorkSimpleVo();
            // 如果是当前用户，返回完整字段
            if (currentUserId != null && currentUserId.equals(work.getCreatorId())) {
               vo.setTitle(work.getTitle());
               vo.setDescription(work.getDescription());
               vo.setCoverUrl(work.getCoverUrl());
               vo.setType(work.getType());
               vo.setPrice(work.getPrice());
               vo.setPublishedAt(work.getPublishedAt());
            } else {
                // 非作者用户，根据visibility过滤
                if (work.getVisibility() == Visibility.PUBLIC) { // 公开
                    // 保留公开字段
                } else {
                    // 非公开作品不返回给非作者
                    return null;
                }
            }
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        Page<WorkSimpleVo> resultPage = new Page<>(workPage.getCurrent(), workPage.getSize(), workPage.getTotal());
        resultPage.setRecords(vos);
        return resultPage;
    }


    /**
     * 上传封面图
     *
     * @param file
     * @param token
     * @return
     */
    @Override
    public String uploadCover(MultipartFile file, String token) {
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

        // 3. 校验图片尺寸（示例：宽高不超过2000px）
        BufferedImage image;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (image.getWidth() > 2000 || image.getHeight() > 2000) {
            throw new IsArgumentException("封面图尺寸不得超过2000x2000像素");
        }

        // 4. 上传到MinIO（封面图独立存储路径）
        String bucketName = "covers";
        String objectKey = "covers/" + UUID.randomUUID() + "." +
                FilenameUtils.getExtension(file.getOriginalFilename());
        storageService.upload(file, bucketName, objectKey);

        // 5. 返回预签名URL（有效期5分钟）
        String coverUrl = storageService.getPreSignedUrl(bucketName, objectKey, 5, TimeUnit.MINUTES);
        return coverUrl;
    }
}
