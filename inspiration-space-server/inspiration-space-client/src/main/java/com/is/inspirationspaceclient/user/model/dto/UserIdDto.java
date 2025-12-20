package com.is.inspirationspaceclient.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(title="UserIdDto", description ="用户id入参")
public class UserIdDto implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(name ="userId", type ="Long", description ="用户id", requiredMode= Schema.RequiredMode.REQUIRED)
        @NotNull
        private Long userId;

        @Schema(name ="token", type ="String", description ="用户token", requiredMode= Schema.RequiredMode.REQUIRED)
        @NotNull
        private String token;
}
