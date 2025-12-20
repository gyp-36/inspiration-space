package com.is.inspirationspaceclient.payment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.is.inspirationspaceclient.payment.model.entity.enums.RefundStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 退款记录表
 * </p>
 */
@Data
@TableName("pay_refunds")
public class PayRefund implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 退款记录ID
     */
    @TableId(value = "refund_id")
    private Long refundId;

    /**
     * 原订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 买家ID
     */
    @TableField("buyer_id")
    private Long buyerId;

    /**
     * 原支付记录ID
     */
    @TableField("transaction_id")
    private Long transactionId;

    /**
     * 商户退款单号，唯一
     */
    @TableField("refund_no")
    private String refundNo;

    /**
     * 第三方退款单号
     */
    @TableField("trade_refund_no")
    private String tradeRefundNo;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 退款状态(0退款中，1成功，2失败，3取消)
     */
    @TableField("status")
    private RefundStatus status;

    /**
     * 退款成功时间
     */
    @TableField("success_time")
    private LocalDateTime successTime;

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