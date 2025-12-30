package com.is.inspirationspaceclient.payment.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "订单视图")
public class OrderPreviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "title", type = "String", description = "标题")
    private String title;

    @Schema(name = "description", type = "String", description = "描述")
    private String description;

    @Schema(name = "coverUrl", type = "String", description = "封面图")
    private String coverUrl;

    @Schema(name = "originalPrice", type = "BigDecimal", description = "价格")
    private BigDecimal originalPrice;

    @Schema(name = "discountPrice", type = "BigDecimal", description = "折扣价格")
    private BigDecimal discountPrice;

    @Schema(name = "totalPrice", type = "BigDecimal", description = "总价")
    private BigDecimal totalPrice;


}
