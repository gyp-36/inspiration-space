package com.is.inspirationspaceclient.chat.model.vo;

import com.is.inspirationspaceclient.chat.model.entity.enums.IsApproval;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "群聊详细信息")
public class GroupInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "群聊名称")
    private String groupName;

    @Schema(description = "群描述")
    private String description;

    @Schema(description = "群头像")
    private String sessionAvatar;

    @Schema(description = "入群是否需审核")
    private IsApproval requiredApproval;

    @Schema(description = "创建者ID(群主)")
    private Long createdBy;
}
