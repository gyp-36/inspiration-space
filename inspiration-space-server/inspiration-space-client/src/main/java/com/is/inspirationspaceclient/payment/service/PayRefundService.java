package com.is.inspirationspaceclient.payment.service;

import com.is.inspirationspaceclient.payment.model.vo.RefundDetailVo;
import com.is.inspirationspaceclient.payment.model.vo.RefundSimpleVo;

public interface PayRefundService {
    Boolean createRefund(Long orderId, String token);

    Boolean cancelRefund(Long refundId, String token);

    RefundSimpleVo listRefund(String token);

    RefundDetailVo getRefundDetail(Long refundId, String token);
}