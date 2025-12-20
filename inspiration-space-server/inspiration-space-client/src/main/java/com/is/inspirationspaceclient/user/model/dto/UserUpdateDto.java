package com.is.inspirationspaceclient.user.model.dto;


import com.is.inspirationspaceclient.user.model.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(title = "UserUpdateDto", description = "用户更新信息")
public class UserUpdateDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "userId", type = "Long", description = "用户ID")
    private Long userId;

    @Schema(name = "username", type = "String", description = "用户名")
    private String username;

    @Schema(name = "email", type = "String", description = "邮箱")
    private String email;


    @Schema(name = "gender", type = "Gender", description = "性别")
    private Gender gender;

    @Schema(name = "birthDate", type = "LocalDate", description = "生日")
    private LocalDate birthDate;

    @Schema(name = "bio", type = "String", description = "简介")
    private String bio;




}
