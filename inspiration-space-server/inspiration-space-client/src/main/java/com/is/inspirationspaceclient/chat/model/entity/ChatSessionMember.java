package com.is.inspirationspaceclient.chat.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.is.inspirationspaceclient.chat.model.entity.enums.IsOnline;
import com.is.inspirationspaceclient.chat.model.entity.enums.MsgRole;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_session_members")
public class ChatSessionMember {

    /**
     * 会话ID
     */
    @TableId("session_id")
    private Long sessionId;

    /**
     * 成员 user_id
     */
   @TableField("user_id")
    private Long userId;

    /**
     * 角色：1:普通成员, 2:管理员, 3:群主
     */
    @TableField("role")
    private MsgRole role;

    /**
     * 最后已读的消息ID（用于未读数计算）
     */
    @TableField("last_read_msg_id")
    private Long lastReadMsgId;

    /**
     * 加入时间
     */
    @TableField("joined_at")
    private LocalDateTime joinedAt;


}