package com.is.inspirationspaceclient.payment.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "支付流程")
public class PaymentInfoVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "TransactionNo",type = "String", description = "系统订单号")
    private String TransactionNo;

    @Schema(name = "base64Image",type = "String", description = "二维码图片")
    private String base64Image;

    @Schema(name = "draftId",type = "String", description = "草稿ID")
    private String draftId;

    @Schema(name = "payTime",type = "LocalDateTime", description = "支付时间")
    private LocalDateTime payTime;


}
