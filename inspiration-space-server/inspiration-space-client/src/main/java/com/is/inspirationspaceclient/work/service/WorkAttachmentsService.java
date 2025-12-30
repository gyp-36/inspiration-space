package com.is.inspirationspaceclient.work.service;


import com.is.inspirationspaceclient.work.model.vo.WorkAttachmentVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 作品附件 服务类
 * </p>
 *
 * @author
 * @since 2025-07-07
 */
public interface WorkAttachmentsService {
    WorkAttachmentVo uploadWorkAttachment(String token, Long workId, MultipartFile file);

    Boolean deleteWorkAttachment(Long attachmentId, String token);

    List<WorkAttachmentVo> getWorkAttachments(String token, Long workId);
}