package com.is.inspirationspaceclient.chat.model.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.is.inspirationspaceclient.chat.model.entity.enums.IsApproval;

import com.is.inspirationspaceclient.chat.model.entity.enums.SessionStatus;
import com.is.inspirationspaceclient.chat.model.entity.enums.SessionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_sessions")
public class ChatSession {

    /**
     * 会话ID
     */
    @TableId("session_id")
    private Long sessionId;

    /**
     * 会话类型：1:群聊, 2:单聊
     */
    @TableField("session_type")
    private SessionType sessionType;

    /**
     * 群聊名称(私聊为用户名)
     */
    @TableField("session_name")
    private String sessionName;

    /**
     * 群描述（单聊忽略）
     */
    @TableField("description")
    private String description;

    /**
     * 群头像
     */
    @TableField("session_avatar")
    private String sessionAvatar;

    /**
     * 入群是否需审核（仅群聊有效）
     */
    @TableField("require_approval")
    private IsApproval requireApproval;

    /**
     * 创建者 user_id（群主）
     */
    @TableField("created_by")
    private Long createdBy;


    /**
     * 会话状态
     */
    @TableField("session_status")
    private SessionStatus sessionStatus;

    /**
     * 创建时间
     */
    @TableField(value = "created_at",fill = FieldFill.INSERT)
    private LocalDateTime createdAt;


}