package com.is.inspirationspaceclient.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.forum.model.entity.ForumComments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ForumCommentsMapper extends BaseMapper<ForumComments> {

    @Update("UPDATE forum_comments SET like_count = like_count + #{delta} WHERE id = #{commentId}")
    int updateLikeCount(@Param("commentId") Long commentId, @Param("delta") int delta);
}
