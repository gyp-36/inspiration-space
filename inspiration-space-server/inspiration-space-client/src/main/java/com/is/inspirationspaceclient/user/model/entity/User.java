package com.is.inspirationspaceclient.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.is.inspirationspaceclient.user.model.entity.enums.Gender;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 用户基本信息表
 */
@Data
@TableName("user_profile")
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /*
    * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 性别
     */
    @TableField("gender")
    private Gender gender;

    /**
     * 生日
     */
    @TableField("birth_date")
    private LocalDate birthDate;

    /**
     * 个性签名
     */
    @TableField("bio")
    private String bio;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 注册时间
     */
    @TableField("register_time")
    private LocalDateTime registerTime;
}
