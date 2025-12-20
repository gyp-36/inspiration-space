package com.is.inspirationspaceclient.chat.model.vo;

import com.is.inspirationspaceclient.chat.model.entity.enums.MsgRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "群成员信息")
public class GroupMemberVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "userId", type = "Long", description = "用户ID")
    private Long userId;

    @Schema(name = "userName", type = "String", description = "用户名称")
    private String userName;

    @Schema(name = "avatar", type = "String", description = "用户头像")
    private String avatar;

    @Schema(name = "role", type = "MsgRole", description = "角色：1-群主, 2-管理员, 3-普通成员")
    private MsgRole role;

}