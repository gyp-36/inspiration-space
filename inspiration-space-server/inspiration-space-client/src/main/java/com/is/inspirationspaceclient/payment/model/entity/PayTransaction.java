package com.is.inspirationspaceclient.payment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.is.inspirationspaceclient.payment.model.entity.enums.PayStatus;
import com.is.inspirationspaceclient.payment.model.entity.enums.PaymentMethod;
import com.is.inspirationspaceclient.payment.model.entity.enums.TradeStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 支付交易记录表
 * </p>
 */
@Data
@TableName("pay_transactions")
public class PayTransaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单草稿ID
     */
    @TableField("draft_id")
    private String draftId;

    /**
     * 交易记录ID
     */
    @TableId(value = "transaction_id")
    private Long transactionId;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 买家ID
     */
    @TableField("buyer_id")
    private Long buyerId;

    /**
     * 支付平台交易号，唯一
     */
    @TableField("transaction_no")
    private String transactionNo;

    /**
     * 支付渠道(1微信，2支付宝,3银行卡，4其他)
     */
    @TableField("payment_method")
    private PaymentMethod paymentMethod;

    /**
     * 本次支付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付状态(0支付中，1成功，2失败，3取消)
     */
    @TableField("status")
    private PayStatus status;

    /**
     * 第三方交易号
     */
    @TableField("trade_no")
    private String tradeNo;

    /**
     * 支付成功时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}