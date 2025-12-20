package com.is.inspirationspaceclient.payment.service;

import com.is.inspirationspaceclient.payment.model.vo.RefundDetailVo;
import com.is.inspirationspaceclient.payment.model.vo.RefundSimpleVo;
import org.springframework.stereotype.Service;

@Service
public class PayRefundServiceImpl implements PayRefundService{
    @Override
    public Boolean createRefund(Long orderId, String token) {
        return null;
    }

    @Override
    public Boolean cancelRefund(Long refundId, String token) {
        return null;
    }

    @Override
    public RefundSimpleVo listRefund(String token) {
        return null;
    }

    @Override
    public RefundDetailVo getRefundDetail(Long refundId, String token) {
        return null;
    }
}
