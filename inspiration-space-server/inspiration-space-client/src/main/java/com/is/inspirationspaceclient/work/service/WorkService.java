package com.is.inspirationspaceclient.work.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.work.model.dto.WorkCreateDto;
import com.is.inspirationspaceclient.work.model.dto.WorkUpdateDto;
import com.is.inspirationspaceclient.work.model.vo.WorkDetailVo;
import com.is.inspirationspaceclient.work.model.vo.WorkSimpleVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 作品表 服务类
 * </p>
 *
 * @author
 * @since 2025-07-07
 */

public interface WorkService  {

    Boolean createDraft(String token, WorkCreateDto workCreateDto);

    Boolean updateDraft(Long workId, String token, WorkCreateDto workCreateDto);

    Boolean publishWork(String token, Long workId);

    Boolean deleteWork(Long workId, String token);

    Boolean updateWork(Long workId, String token, WorkUpdateDto workUpdateDto);

    WorkDetailVo getWorkDetail(String token, Long workId);

    Page<WorkSimpleVo> getUserWorks(Long userId, int page, int size);

    String uploadCover(MultipartFile file, String token);
}