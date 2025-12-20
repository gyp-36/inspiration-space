package com.is.inspirationspaceclient.payment.model.vo;

import com.is.inspirationspaceclient.payment.model.entity.enums.TradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(name = "PaymentVo", description = "支付回调信息")
public class PaymentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "tradeNo", type = "String", description = "第三方交易号")
    private String tradeNo;

    @Schema(name = "tradeStatus", type = "TradeStatus", description = "第三方交易状态")
    private TradeStatus tradeStatus;

    @Schema(name = "payTime", type = "LocalDateTime", description = "支付时间")
    private LocalDateTime payTime;

}
