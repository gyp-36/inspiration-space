package com.is.inspirationspaceclient.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.is.inspirationspaceclient.chat.model.entity.ChatSessionMember;
import com.is.inspirationspaceclient.chat.model.entity.enums.SessionStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatSessionMemberMapper extends BaseMapper<ChatSessionMember> {
    @Insert("")
    void batchInsertMembers(@Param("sessionId") Long sessionId, @Param("userIds") List<Long> userIds);

    @Select("SELECT COUNT(*) FROM chat_session_members csm " +
            "LEFT JOIN chat_sessions cs ON csm.session_id = cs.session_id " +
            "WHERE csm.session_id = #{sessionId} " +
            "AND csm.user_id = #{userId} " +
            "AND cs.session_status = #{sessionStatus}")
    Integer selectValidSessionMemberCount(@Param("sessionId") Long sessionId,
                                          @Param("userId") Long userId,
                                          @Param("sessionStatus") SessionStatus sessionStatus);

}
