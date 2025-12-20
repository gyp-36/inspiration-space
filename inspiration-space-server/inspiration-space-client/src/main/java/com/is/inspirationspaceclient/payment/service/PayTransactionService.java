package com.is.inspirationspaceclient.payment.service;

import com.is.inspirationspaceclient.payment.model.dto.PaymentDto;
import com.is.inspirationspaceclient.payment.model.entity.PayTransaction;

public interface PayTransactionService {
    String generatePayOrder(String token, String draftId, PaymentDto paymentDto);

    Boolean cancelPay(String token, Long tractionId);

    Boolean updatePayStatus(String tradeNo);
}