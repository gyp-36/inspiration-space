package com.is.inspirationspaceclient.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.payment.model.entity.PayTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayTransactionMapper extends BaseMapper<PayTransaction> {
    PayTransaction selectByTradeNo(String tradeNo);
}
