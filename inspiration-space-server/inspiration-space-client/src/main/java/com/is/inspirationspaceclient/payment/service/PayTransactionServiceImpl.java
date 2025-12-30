package com.is.inspirationspaceclient.payment.service;

import com.is.inspirationspaceclient.payment.mapper.PayTransactionMapper;
import com.is.inspirationspaceclient.payment.model.dto.PaymentDto;
import com.is.inspirationspaceclient.payment.model.entity.PayTransaction;
import com.is.inspirationspaceclient.payment.model.entity.enums.PayStatus;

import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.exception.IsServiceException;
import com.is.inspirationspacecommon.redis.RedisCache;
import com.is.inspirationspacecommon.redis.RedisKeyBuild;
import com.is.inspirationspacecommon.redis.RedisKeyManage;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@Slf4j
public class PayTransactionServiceImpl implements PayTransactionService{

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Autowired
    private PayTransactionMapper payTransactionMapper;

    /**
     * 生成支付订单
     *
     * @param token
     * @param draftId
     * @param paymentDto
     * @return
     */
    @Override
    public String generatePayOrder(String token, String draftId, PaymentDto paymentDto) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        //1.权限校验
        String draftUserId = redisCache.getHash(RedisKeyBuild.createRedisKey(RedisKeyManage.ORDER_DRAFT, draftId), "userId", String.class);
        if (!String.valueOf(userId).equals(draftUserId)) {
            throw new IsServiceException(ErrorCode.FORBIDDEN.getHttpStatusCode(), "无权操作他人订单");
        }

        //2.生成支付订单
        PayTransaction payTransaction = new PayTransaction();
        payTransaction.setTransactionId(snowflakeIdGenerator.nextId());
        payTransaction.setDraftId(draftId);
        payTransaction.setOrderId(snowflakeIdGenerator.nextId());
        payTransaction.setBuyerId(userId);
        payTransaction.setTransactionNo(generateOrderNo(userId));
        payTransaction.setPaymentMethod(paymentDto.getPaymentMethod());
        payTransaction.setAmount(paymentDto.getAmount());
        payTransaction.setStatus(PayStatus.UNPAY);
        payTransaction.setTradeNo(String.valueOf((snowflakeIdGenerator.nextId()))); // 支付完成前，第三方交易号为空

        //3.保存支付订单
        payTransactionMapper.insert(payTransaction);
        log.info("买家{}，有一条新的支付订单，单号：{}", payTransaction.getBuyerId(), payTransaction.getTransactionId());
        return payTransaction.getTransactionNo();
    }

    @Override
    public Boolean cancelPay(String token, Long tractionId) {
        return null;
    }

    @Override
    public Boolean updatePayStatus(String transactionNo) {
        //1.查询支付订单
        PayTransaction payTransaction = payTransactionMapper.selectByTransactionNo(transactionNo);
        if (payTransaction == null) {
            log.error("支付状态更新失败：找不到单号为 {} 的支付订单", transactionNo);
            throw new IsServiceException(ErrorCode.PAYMENT_NOT_FOUND.getHttpStatusCode(), "支付订单不存在");
        }

        //2.更新状态
        payTransaction.setStatus(PayStatus.SUCCESS);
        // 注意：这里 transactionNo 是我们的内部单号，第三方流水号如果需要记录，应该从回调参数中获取并设置到 tradeNo 字段
        payTransaction.setPayTime(LocalDateTime.now());

        return payTransactionMapper.updateById(payTransaction)>0;
    }

    private String generateOrderNo(long userId) {
        // 建议格式：日期+用户ID后4位+随机数
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String userIdPart = String.format("%04d", userId % 10000);
        String randomPart = String.format("%06d", new Random().nextInt(999999));
        return "ORD" + datePart + userIdPart + randomPart;
    }
}
