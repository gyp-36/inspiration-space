package com.is.inspirationspaceclient.user.model.vo;

import cn.hutool.core.util.DesensitizedUtil;

import com.is.inspirationspaceclient.user.model.entity.enums.Gender;
import com.is.inspirationspacecommon.util.StringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Schema(title = "UserProfileVo", description = "用户基本信息")
public class UserProfileVo {
    @Schema(name = "id", type = "Long", description = "用户id")
    private Long userId;

    @Schema(name = "username", type = "String", description = "用户名")
    private String username;

    @Schema(name = "email", type = "String", description = "邮箱")
    private String email;

    @Schema(name = "phone", type = "String", description = "手机号")
    private String phone;

    @Schema(name = "gender", type = "Gender", description = "性别")
    private Gender gender;

    @Schema(name = "birthDate", type = "LocalDate", description = "生日")
    private LocalDate birthDate;

    @Schema(name = "registerTime", type = "LocalDateTime", description = "注册时间")
    private LocalDateTime registerTime;


    public String getMobile() {
        if (StringUtil.isNotEmpty(phone)) {
            return DesensitizedUtil.mobilePhone(phone);
        }else {
            return phone;
        }
    }
}
