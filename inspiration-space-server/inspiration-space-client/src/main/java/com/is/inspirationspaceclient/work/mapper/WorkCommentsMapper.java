package com.is.inspirationspaceclient.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.work.model.entity.WorkComments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WorkCommentsMapper extends BaseMapper<WorkComments> {
    @Update("UPDATE work_comments SET like_count = like_count + #{delta} WHERE id = #{commentId}")
    int updateLikeCount(@Param("commentId") Long commentId, @Param("delta") int delta);
}
