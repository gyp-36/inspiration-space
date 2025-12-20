package com.is.inspirationspaceclient.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.forum.model.entity.ForumUserActions;
import com.is.inspirationspaceclient.forum.model.entity.enums.TargetType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ForumUserActionsMapper extends BaseMapper<ForumUserActions> {
    @Delete("DELETE FROM forum_user_actions WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    int deleteByUserTarget(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") TargetType targetType);

}
