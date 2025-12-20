package com.is.inspirationspaceclient.user.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "username", type = "String", description = "用户名")
    private String username;

    @Schema(name = "email", type = "String", description = "邮箱")
    private String email;

    @Schema(name = "mobile", type = "String", description = "手机号")
    private String mobile;

    @Schema(name = "gender", type = "String", description = "性别")
    private String gender;

    @Schema(name = "birthDate", type = "LocalDate", description = "生日")
    private LocalDate birthDate;

    @Schema(name = "bio", type = "String", description = "简介")
    private String bio;

    @Schema(name = "avatarUrl", type = "String", description = "头像URL")
    private String avatarUrl;

}
