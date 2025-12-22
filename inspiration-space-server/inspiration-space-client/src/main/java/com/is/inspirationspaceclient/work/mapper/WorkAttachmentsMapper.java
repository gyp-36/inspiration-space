package com.is.inspirationspaceclient.work.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.work.model.entity.WorkAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 作品附件 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2025-07-07
 */
@Mapper
public interface WorkAttachmentsMapper extends BaseMapper<WorkAttachment> {

    @Select("SELECT * FROM work_attachment WHERE work_id = #{workId}")
    List<WorkAttachment> selectByWorkId(Long workId);
}
