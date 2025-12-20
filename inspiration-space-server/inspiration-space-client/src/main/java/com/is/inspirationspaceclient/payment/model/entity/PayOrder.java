package com.is.inspirationspaceclient.payment.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.is.inspirationspaceclient.payment.model.entity.enums.ProductType;
import com.is.inspirationspaceclient.payment.model.entity.enums.TradeStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单主表
 * </p>
 */
@Data
@TableName("pay_orders")
public class PayOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "order_id")
    private Long orderId;

    /**
     * 商户系统唯一订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 第三方交易号
     */
    @TableField("trade_no")
    private String tradeNo;

    /**
     * 下单用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 订单标题
     */
    @TableField("subject")
    private String subject;

    /**
     * 订单描述
     */
    @TableField("content")
    private String content;

    /**
     * 原金额（单位：元）
     */
    @TableField("original_amount")
    private BigDecimal originalAmount;

    /**
     * 优惠金额（单位：元）
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 总金额（单位：元）
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 产品类型：1=会员, 2=商品
     */
    @TableField("product_type")
    private ProductType productType;

    /**
     * 交易状态（0创建，1未支付，2支付中，3已支付，4-退款 5-已删除）
     */
    @TableField("trade_status")
    private TradeStatus tradeStatus;

    /**
     * 订单过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

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

    /**
     * 关闭时间（超时未付）
     */
    @TableField("expired_at")
    private LocalDateTime expiredAt;

    /**
     * 支付完成时间
     */
    @TableField("paid_at")
    private LocalDateTime paidAt;
}