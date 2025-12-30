package com.is.inspirationspaceclient.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.is.inspirationspaceclient.payment.model.dto.PaymentDto;
import com.is.inspirationspaceclient.payment.model.dto.PaymentNotifyDto;
import com.is.inspirationspaceclient.payment.model.vo.*;
import com.is.inspirationspaceclient.payment.service.PayOrderService;

import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay-order")
@AllArgsConstructor
@Tag(name = "支付接口", description = "支付接口")
public class PayOrderController {

    @Autowired
    private PayOrderService payOrderService;

    //=======================下单流程=======================
    //预览订单
    @Operation(summary = "预览订单")
    @GetMapping("/preview")
    public ApiResponse<OrderPreviewVo> previewOrder(@RequestHeader("Authorization") String token, @RequestParam Long workId) {
        return ApiResponse.ok(payOrderService.createPreviewOrder(token, workId));
    }

    //创建订单草稿
    @Operation(summary = "创建订单草稿")
    @PostMapping("/createDraft")
    public ApiResponse<OrderDraftVo> createOrderDraft(@RequestHeader("Authorization") String token, @Valid @RequestParam Long workId) {
        return ApiResponse.ok(payOrderService.createOrderDraft(token, workId));
    }

    //取消订单
    @Operation(summary = "取消订单")
    @PatchMapping("/cancel")
    public ApiResponse<Boolean> cancelOrder(@RequestHeader("Authorization") String token, @RequestParam String draftId) {
        return ApiResponse.ok(payOrderService.cancelOrderDraft(token, draftId));
    }

    //=======================支付流程=======================
    //支付接口->生成支付订单->支付成功->支付通知->生成订单
    @Operation(summary = "支付接口")
    @PostMapping("/pay")
    public ApiResponse<PaymentInfoVo> pay(@RequestHeader("Authorization") String token, @Valid @RequestBody PaymentDto paymentDto) {
        return ApiResponse.ok(payOrderService.pay(token, paymentDto.getDraftId(), paymentDto));
    }

    @Operation(summary = "支付通知")
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        Boolean result = payOrderService.payNotify(request, null);
        return result ? "success" : "fail";
    }

    //生成订单
    @Operation(summary = "生成订单")
    @PostMapping("/generate")
    public ApiResponse<Boolean> generateOrder(@Valid @RequestParam String tradeNo,@RequestParam PaymentNotifyDto paymentNotifyDto) {
        return ApiResponse.ok(payOrderService.generateOrder(tradeNo,paymentNotifyDto));
    }



    //=======================查询订单=======================
    //查询订单简略(分页)
    @Operation(summary = "查询订单(分页)")
    @GetMapping("/simple")
    public ApiResponse<Page<OrderSimpleVo>> listOrder(@RequestHeader("Authorization") String token, @RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(payOrderService.listOrder(token, pageNum, pageSize));
    }

    //查询订单详情
    @Operation(summary = "查询订单详情")
    @GetMapping("/detail")
    public ApiResponse<OrderDetailVo> getOrderDetail(@RequestHeader("Authorization") String token, @RequestParam Long orderId) {
        return ApiResponse.ok(payOrderService.getOrderDetail(token, orderId));
    }


}
