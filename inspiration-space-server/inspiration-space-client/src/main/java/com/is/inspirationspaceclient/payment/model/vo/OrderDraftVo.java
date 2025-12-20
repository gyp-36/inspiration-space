package com.is.inspirationspaceclient.payment.model.vo;

import com.is.inspirationspaceclient.payment.model.entity.enums.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "订单视图")
public class OrderDraftVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "draftId",type = "String",description = "草稿ID")
    private String draftId;

    @Schema(name = "userId",type = "Long",description = "下单用户ID")
    private Long userId;

    @Schema(name = "workId",type = "Long",description = "作品ID")
    private Long workId;

    @Schema(name = "productId",type = "Long",description = "商品ID")
    private Long productId;

    @Schema(name = "subject",type = "String",description = "订单标题")
    private String subject;

    @Schema(name = "content",type = "String",description = "订单描述")
    private String content;

    @Schema(name = "originalAmount",type = "BigDecimal",description = "原金额（单位：元）")
    private BigDecimal originalAmount;

    @Schema(name = "discountAmount",type = "BigDecimal",description = "优惠金额（单位：元）")
    private BigDecimal discountAmount;

    @Schema(name = "totalAmount",type = "BigDecimal",description = "总金额（单位：元）")
    private BigDecimal totalAmount;

    @Schema(name = "productType",type = "ProductType",description = "产品类型：1=会员, 2=商品")
    private ProductType productType;

    @Schema(name = "expireTime",type = "Integer",description = "订单过期时间")
    private Integer expireTime;
}
