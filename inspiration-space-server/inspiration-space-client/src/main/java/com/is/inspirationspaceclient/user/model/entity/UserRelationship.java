package com.is.inspirationspaceclient.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.is.inspirationspaceclient.user.model.entity.enums.FollowType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_relationship")
public class UserRelationship implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关注者ID
     */
    @TableId("follower_id")
    private Long followerId;

    /**
     * 被关注者ID
     */
    @TableField("followee_id")
    private Long followeeId;

    /**
     * 关注类型
     */
    @TableField("follow_type")
    private FollowType followType;

    /**
     * 关注时间
     */
    @TableField("follow_time")
    private LocalDateTime followTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
