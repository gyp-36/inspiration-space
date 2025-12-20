package com.is.inspirationspaceclient.payment.controller;

import com.is.inspirationspaceclient.payment.model.vo.RefundDetailVo;
import com.is.inspirationspaceclient.payment.model.vo.RefundSimpleVo;
import com.is.inspirationspaceclient.payment.service.PayRefundService;

import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay-refund")
public class PayRefundController {

    @Autowired
    private PayRefundService payRefundService;

    //发起退款
    @RequestMapping("/create")
    @Operation(summary = "发起退款")
    public ApiResponse<Boolean> createRefund(@Valid @RequestParam Long orderId, @RequestHeader("Authorization") String token) {

        return ApiResponse.ok(payRefundService.createRefund(orderId, token));
    }

    //取消退款
    @RequestMapping("/cancel")
    @Operation(summary = "取消退款")
    public ApiResponse<Boolean> cancelRefund(@Valid @RequestParam Long refundId, @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(payRefundService.cancelRefund(refundId, token));
    }

    //查询退款记录
    @RequestMapping("/list")
    @Operation(summary = "查询退款记录")
    public ApiResponse<RefundSimpleVo> listRefund(@RequestHeader("Authorization") String token) {
        return ApiResponse.ok(payRefundService.listRefund(token));
    }

    //查询退款详情
    @RequestMapping("/detail")
    @Operation(summary = "查询退款详情")
    public ApiResponse<RefundDetailVo> getRefundDetail(@Valid @RequestParam Long refundId, @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(payRefundService.getRefundDetail(refundId, token));
    }
}
