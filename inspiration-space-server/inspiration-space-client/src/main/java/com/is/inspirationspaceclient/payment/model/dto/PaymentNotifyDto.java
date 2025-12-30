package com.is.inspirationspaceclient.payment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PaymentNotifyDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "draftId", type = "String", description = "草稿Id" )
    private String draftId;

    @Schema(name = "transactionNo", type = "String", description = "第三方交易号" )
    private String transactionNo;


    @Schema(name = "payTime", type = "LocalDateTime", description = "支付时间" )
    private LocalDateTime payTime;
}
