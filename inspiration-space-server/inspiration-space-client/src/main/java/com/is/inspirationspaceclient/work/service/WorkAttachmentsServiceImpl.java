package com.is.inspirationspaceclient.work.service;



import com.is.inspirationspaceclient.work.mapper.WorkAttachmentsMapper;
import com.is.inspirationspaceclient.work.mapper.WorkInfoMapper;
import com.is.inspirationspaceclient.work.mapper.WorkStatsMapper;
import com.is.inspirationspaceclient.work.model.entity.WorkAttachment;
import com.is.inspirationspaceclient.work.model.entity.WorkInfo;
import com.is.inspirationspaceclient.work.model.vo.WorkAttachmentVo;
import com.is.inspirationspacecommon.config.StorageService;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.exception.IsSystemException;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 作品附件 服务实现类
 * </p>
 *
 * @author
 * @since 2025-07-07
 */
@Service
@Slf4j
public class WorkAttachmentsServiceImpl implements WorkAttachmentsService {

    @Autowired
    private WorkAttachmentsMapper workAttachmentsMapper;

    @Autowired
    private StorageService storageService;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private WorkInfoMapper workInfoMapper;
    @Autowired
    private WorkStatsMapper workStatsMapper;


    @Override
    public WorkAttachmentVo uploadWorkAttachment(String token, Long workId, MultipartFile file) {
        //1.解析token,获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) throw new IsArgumentException("无效token");

        // 2. 上传文件到MinIO（普通附件独立存储路径）
        String bucketName = "attachments";
        String objectKey = "attachments/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        storageService.upload(file, bucketName, objectKey);

        // 3. 计算文件哈希
        String fileHash;
        try {
            fileHash = DigestUtils.sha256Hex(file.getInputStream());
        } catch (IOException e) {
            throw new IsArgumentException("文件处理失败");
        }

        // 4. 生成业务ID（雪花ID）
        Long attachmentId = snowflakeIdGenerator.nextId();

        // 5. 保存附件元数据到 work_attachment 表（此时work_id仍为空）
        WorkAttachment attachment = new WorkAttachment();
        attachment.setId(attachmentId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setFileHash(fileHash);
        attachment.setBucketName(bucketName);
        attachment.setObjectKey(objectKey);
        workAttachmentsMapper.insert(attachment);

        //6. 返回附件基础数据
        WorkAttachmentVo workAttachmentVo = new WorkAttachmentVo();
        workAttachmentVo.setFileName(file.getOriginalFilename());
        workAttachmentVo.setFileSize(file.getSize());
        workAttachmentVo.setFileExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
        return workAttachmentVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWorkAttachment(Long attachmentId, String token) {
        // 1. 解析token并获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 获取附件记录
        WorkAttachment attachment = workAttachmentsMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new IsArgumentException(ErrorCode.ATTACHMENT_NOT_FOUND.getHttpStatusCode(), "附件不存在");
        }

        // 3. 获取关联作品并验证权限
        WorkInfo workInfo = workInfoMapper.selectById(attachment.getWorkId());
        if (workInfo == null) {
            throw new IsArgumentException(ErrorCode.WORK_NOT_FOUND.getHttpStatusCode(), "关联作品不存在");
        }
        if (!workInfo.getCreatorId().equals(userId)) {
            throw new IsArgumentException(ErrorCode.PERMISSION_DENIED.getHttpStatusCode(), "无权删除非本人作品的附件");
        }
        if (workInfo.getDeletedAt() != null) {
            throw new IsArgumentException(ErrorCode.WORK_ALREADY_DELETED.getHttpStatusCode(), "关联作品已被删除");
        }

        // 4. 删除MinIO中的文件（物理删除）
        try {
            storageService.delete(attachment.getBucketName(), attachment.getObjectKey());
        } catch (Exception e) {
            log.error("MinIO文件删除失败 | attachmentId={}, objectKey={}",
                    attachmentId, attachment.getObjectKey(), e);
            throw new IsSystemException("文件删除失败，请稍后重试");
        }

        // 5. 删除数据库记录
        workAttachmentsMapper.deleteById(attachmentId);

        // 6. 异步发送消息（通知附件已删除）
//        messageService.sendAttachmentDeletedMessage(attachmentId, userId);

        return true;
    }

    @Override
    public List<WorkAttachmentVo> getWorkAttachments(Long workId) {
        //1.

        return List.of();
    }

}
