package com.is.inspirationspaceclient.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.payment.model.entity.PayTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PayTransactionMapper extends BaseMapper<PayTransaction> {
    @Select("SELECT * FROM pay_transactions WHERE transaction_no = #{transactionNo}")
    PayTransaction selectByTransactionNo(@Param("transactionNo") String transactionNo);
}
