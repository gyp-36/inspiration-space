package com.is.inspirationspaceclient.payment.model.dto;

import com.is.inspirationspaceclient.payment.model.entity.enums.PayStatus;
import com.is.inspirationspaceclient.payment.model.entity.enums.PaymentMethod;
import com.is.inspirationspaceclient.payment.model.entity.enums.TradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(name = "PaymentDto", description = "支付信息")
public class PaymentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "subject", type = "String", description = "订单标题")
    private String subject;


    @Schema(name = "tradeNo", type = "String", description = "第三方交易号")
    private String tradeNo;

    @Schema(name = "paymentMethod", type = "PaymentMethod", description = "支付渠道")
    private PaymentMethod paymentMethod;

    @Schema(name = "payStatus", type = "String", description = "第三方交易状态")
    private PayStatus payStatus;

    @Schema(name = "amount", type = "BigDecimal", description = "支付金额")
    private BigDecimal amount;




}
