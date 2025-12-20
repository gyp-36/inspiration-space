package com.is.inspirationspaceclient.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.is.inspirationspaceclient.user.model.entity.enums.UserStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * 用户扩展信息表
 */
@Data
@TableName("user_expansion")
public class UserExpansion implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId("user_id")
    private Long userId;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 账户余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /*
    * 信用分
     */
    @TableField("credit_score")
    private Integer creditScore;

    /**
     * 用户状态
     */
    @TableField("user_status")
    private UserStatus userStatus;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    private Boolean deleted;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
