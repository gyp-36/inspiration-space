package com.is.inspirationspaceclient.chat.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.is.inspirationspaceclient.chat.model.entity.enums.MsgStatus;
import com.is.inspirationspaceclient.chat.model.entity.enums.MsgType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_messages")
public class ChatMessage {

    /**
     * 消息ID（雪花ID）
     */
    @TableId(type = IdType.INPUT)
    private Long messageId;

    /**
     * 所属会话ID
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * 发送者 user_id
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息类型：1:文本, 2:图片, 3:文件, 4:系统消息 5:群公告
     */
    private MsgType msgType;



    /**
     * 文件 URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 状态：1:正常, 2:已撤回  3.已送达
     */
    private MsgStatus status;

    /**
     * 发送时间
     */
    @TableField(value = "sent_at", fill = FieldFill.INSERT)
    private LocalDateTime sentAt;

    /**
     * 更新时间
     */
    @TableField(value = "update_at", fill = FieldFill.UPDATE)
    private LocalDateTime updateAt;
}