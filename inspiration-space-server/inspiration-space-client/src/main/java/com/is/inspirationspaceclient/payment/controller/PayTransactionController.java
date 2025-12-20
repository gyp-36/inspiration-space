package com.is.inspirationspaceclient.payment.controller;

import com.is.inspirationspaceclient.payment.model.dto.PaymentDto;
import com.is.inspirationspaceclient.payment.model.vo.PayTransactionVo;
import com.is.inspirationspaceclient.payment.service.PayTransactionService;

import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay-transaction")
@AllArgsConstructor
@Tag(name = "支付接口", description = "支付接口")
public class PayTransactionController {

    private PayTransactionService payTransactionService;




    //生成支付订单
    @Operation(summary = "生成支付订单")
    @PostMapping("/generate")
    public ApiResponse<String> generatePayOrder(
            @RequestHeader("Authorization") String token,
            @RequestParam String draftId,
            @RequestParam PaymentDto paymentDto
    ) {
        return ApiResponse.ok(payTransactionService.generatePayOrder(token, draftId, paymentDto));
    }

    @Operation(summary = "更新订单状态")
    @PatchMapping("/updateStatus")
    public ApiResponse<Boolean> updateStatus(@RequestParam String transactionNo) {
        return ApiResponse.ok(payTransactionService.updatePayStatus(transactionNo));
    }


    //取消支付
    @Operation(summary = "取消支付")
    @PatchMapping("/cancelPay")
    public ApiResponse<Boolean> cancelPay(@RequestHeader("Authorization") String token, @RequestParam Long tractionId) {
        return ApiResponse.ok(payTransactionService.cancelPay(token, tractionId));
    }
}
