package com.is.inspirationspaceclient.user.model.vo;

import com.is.inspirationspaceclient.user.model.entity.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(title = "UserExtendVo", description = "用户扩展信息")
public class UserExtendVo {

    @Schema(name = "balance", type = "BigDecimal", description = "余额")
    private BigDecimal balance;

    @Schema(name = "credit_score", type = "Integer", description = "信誉分")
    private Integer creditScore;

    @Schema(name = "user_status", type = "Integer", description = "用户状态")
    private UserStatus userStatus;

}
