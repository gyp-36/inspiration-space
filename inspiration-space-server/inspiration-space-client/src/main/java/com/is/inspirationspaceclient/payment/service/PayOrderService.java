package com.is.inspirationspaceclient.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.is.inspirationspaceclient.payment.model.dto.PaymentDto;
import com.is.inspirationspaceclient.payment.model.dto.PaymentNotifyDto;
import com.is.inspirationspaceclient.payment.model.vo.*;
import jakarta.servlet.http.HttpServletRequest;

public interface PayOrderService {
    OrderDraftVo createOrderDraft(String token, Long workId);

    Boolean cancelOrderDraft(String token, String orderId);

    Page<OrderSimpleVo> listOrder(String token, int pageNum, int pageSize);

    OrderDetailVo getOrderDetail(String token, Long orderId);

    OrderPreviewVo createPreviewOrder(String token, Long workId);

    Boolean generateOrder(String tradeNo, PaymentNotifyDto paymentNotifyDto);

    PaymentInfoVo pay(String token, String draftId, PaymentDto paymentDto);

    Boolean payNotify(HttpServletRequest request, PaymentNotifyDto paymentNotifyDto);
}