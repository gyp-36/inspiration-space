package com.is.inspirationspaceclient.user.model.vo;

import cn.hutool.core.util.DesensitizedUtil;


import com.is.inspirationspaceclient.user.model.entity.enums.Gender;
import com.is.inspirationspacecommon.util.StringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(title = "UserUpdateVo", description = "用户更新信息")
public class UserUpdateVo {

    @Schema(name = "username", type = "String", description = "用户名")
    private String username;

    @Schema(name = "email", type = "String", description = "邮箱")
    private String email;

    @Schema(name = "mobile", type = "String", description = "手机号")
    private String mobile;

    @Schema(name = "gender", type = "Gender", description = "性别")
    private Gender gender;

    @Schema(name = "birthDate", type = "LocalDate", description = "生日")
    private LocalDate birthDate;

    @Schema(name = "bio", type = "String", description = "简介")
    private String bio;

    @Schema(name = "avatarUrl", type = "String", description = "头像URL")
    private String avatarUrl;



    public String getMobile() {
        if (StringUtil.isNotEmpty(mobile)) {
            return DesensitizedUtil.mobilePhone(mobile);
        }else {
            return mobile;
        }
    }
}
