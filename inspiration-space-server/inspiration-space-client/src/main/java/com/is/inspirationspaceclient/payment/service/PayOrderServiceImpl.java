package com.is.inspirationspaceclient.payment.service;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.fastjson.JSONObject;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.is.inspirationspaceclient.payment.mapper.PayOrderMapper;
import com.is.inspirationspaceclient.payment.mapper.PayTransactionMapper;
import com.is.inspirationspaceclient.payment.model.dto.PaymentDto;
import com.is.inspirationspaceclient.payment.model.dto.PaymentNotifyDto;
import com.is.inspirationspaceclient.payment.model.entity.PayOrder;
import com.is.inspirationspaceclient.payment.model.entity.enums.ProductType;
import com.is.inspirationspaceclient.payment.model.entity.enums.TradeStatus;
import com.is.inspirationspaceclient.payment.model.vo.*;

import com.is.inspirationspaceclient.work.mapper.WorkInfoMapper;
import com.is.inspirationspaceclient.work.model.entity.WorkInfo;
import com.is.inspirationspaceclient.work.model.entity.enums.Status;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.exception.IsServiceException;
import com.is.inspirationspacecommon.redis.RedisCache;
import com.is.inspirationspacecommon.redis.RedisKeyBuild;
import com.is.inspirationspacecommon.redis.RedisKeyManage;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements PayOrderService {

    private static final int ORDER_EXPIRE_MINUTES = 15; // 订单过期时间(分钟)

    private final PayTransactionMapper transactionMapper;

    private final WorkInfoMapper workInfoMapper;

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    private final RedisCache redisCache;

    private final Config config;

    private final PayTransactionService payTransactionService;
    private final PayOrderMapper payOrderMapper;


    //=======================下单流程=======================

    /**
     * 创建订单预览数据
     *
     * @param token
     * @param workId
     * @return
     */
    @Override
    public OrderPreviewVo createPreviewOrder(String token, Long workId) {
        // 1. 获取用户ID（仅验证权限，不创建任何记录）
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        // 2. 校验商品是否存在（只读操作）
        WorkInfo workInfo = workInfoMapper.selectById(workId);
        if (workInfo == null || workInfo.getStatus() != Status.PUBLISHED) {
            throw new IsServiceException(ErrorCode.INVALID_PARAMETER.getHttpStatusCode(), "作品不存在或已下架");
        }

        // 3. 构建预览数据（不生成订单ID，不存Redis）
        OrderPreviewVo orderPreviewVo = new OrderPreviewVo();
        orderPreviewVo.setTitle(workInfo.getTitle());
        orderPreviewVo.setDescription(workInfo.getDescription());
        orderPreviewVo.setOriginalPrice(workInfo.getPrice());
        orderPreviewVo.setDiscountPrice(BigDecimal.ZERO);
        orderPreviewVo.setTotalPrice(workInfo.getPrice().subtract(orderPreviewVo.getDiscountPrice()));


        return orderPreviewVo;
    }

    /**
     * 创建订单
     *
     * @param token
     * @param workId
     * @return
     */
    @Override
    public OrderDraftVo createOrderDraft(String token, Long workId) {
        // 1. 获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }


        // 2.防止重复创建订单(5s内)
        RedisKeyBuild requestId = RedisKeyBuild.createRedisKey(RedisKeyManage.ORDER_DRAFT_REQUEST, userId, workId);
        if (redisCache.hasKey(requestId)) {
            throw new IsServiceException(ErrorCode.INVALID_PARAMETER.getHttpStatusCode(), "请勿重复提交订单");
        }
        redisCache.set(requestId, "1", 5, TimeUnit.SECONDS);


        //3.创建订单（分布式锁）
        String lockKey = "order_draft_lock:" + userId;
        try {
            if (!redisCache.tryLock(lockKey, 3, 10, TimeUnit.SECONDS)) {
                throw new IsServiceException(ErrorCode.SERVICE_UNAVAILABLE.getHttpStatusCode(), "操作太频繁，请稍后再试");
            }

            // 1. 校验商品是否存在
            WorkInfo workInfo = workInfoMapper.selectById(workId);
            if (workId == null || workInfo == null || workInfo.getStatus() != Status.PUBLISHED) {
                throw new IsServiceException(ErrorCode.INVALID_PARAMETER.getHttpStatusCode(), "作品不存在或已下架");
            }

            // 2. 生成草稿ID
            String draftId = generateDraftId(userId);

            // 3. 构建草稿数据
            Map<String, String> draftData = new HashMap<>();
            draftData.put("userId", String.valueOf(userId));
            draftData.put("workId", String.valueOf(workId));
            draftData.put("subject", workInfo.getTitle());
            draftData.put("content", workInfo.getDescription());
            draftData.put("originalAmount", workInfo.getPrice().toString());
            draftData.put("discountAmount", "0");
            draftData.put("totalAmount", workInfo.getPrice().toString());
            draftData.put("productType", ProductType.PRODUCT.getDesc());
            draftData.put("tradeStatus", TradeStatus.CREATED.getDesc());
            draftData.put("expireTime", LocalDateTime.now().plusMinutes(ORDER_EXPIRE_MINUTES).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 4. 保存到Redis（使用草稿专用key）
            RedisKeyBuild draftKey = RedisKeyBuild.createRedisKey(
                    RedisKeyManage.ORDER_DRAFT, draftId);

            redisCache.putHashAll(
                    draftKey,
                    draftData,
                    ORDER_EXPIRE_MINUTES,
                    TimeUnit.MINUTES
            );


            // 4. 构建视图
            OrderDraftVo draftVo = new OrderDraftVo();
            draftVo.setDraftId(draftId);
            draftVo.setUserId(userId);
            draftVo.setWorkId(workId);
            draftVo.setSubject(workInfo.getTitle());
            draftVo.setContent(workInfo.getDescription());
            draftVo.setOriginalAmount(workInfo.getPrice());
            draftVo.setDiscountAmount(BigDecimal.ZERO);
            draftVo.setTotalAmount(workInfo.getPrice());
            draftVo.setProductType(ProductType.PRODUCT);
            draftVo.setExpireTime(ORDER_EXPIRE_MINUTES * 60);  // 剩余秒数

            return draftVo;
        } finally {
            redisCache.unlock(lockKey);
        }

    }


    /**
     * 取消订单
     *
     * @param token
     * @param draftId
     * @return
     */
    @Override
    public Boolean cancelOrderDraft(String token, String draftId) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }
        // 1. 使用正确的Redis Key（订单草稿）
        RedisKeyBuild draftKey = RedisKeyBuild.createRedisKey(RedisKeyManage.ORDER_DRAFT, draftId);

        // 2. 检查订单是否存在
        if (!redisCache.hasKey(draftKey)) {
            throw new IsServiceException(ErrorCode.INVALID_PARAMETER.getHttpStatusCode(), "订单不存在");
        }

        // 3. 权限验证：确保用户 owns this draft
        String orderUserId = redisCache.getHash(draftKey, "userId", String.class);
        if (!String.valueOf(userId).equals(orderUserId)) {
            throw new IsServiceException(ErrorCode.FORBIDDEN.getHttpStatusCode(), "无权操作他人订单");
        }

        // 4. 状态检查：只允许取消CREATED状态的草稿
        String status = redisCache.getHash(draftKey, "tradeStatus", String.class);
        if (!TradeStatus.CREATED.name().equals(status)) {
            throw new IsServiceException(ErrorCode.INVALID_PARAMETER.getHttpStatusCode(),
                    "订单状态不允许取消（当前状态: " + status + "）");
        }
        // 5. 删除订单草稿
        redisCache.del(draftKey);
        log.info("用户{}订单草稿已取消：{}", userId, draftId);

        return true;
    }
    //=======================支付流程=======================


    /**
     * 支付
     *
     * @param token
     * @param draftId
     * @param paymentDto
     * @return
     */
    @SneakyThrows
    @Override
    public PaymentInfoVo pay(String token, String draftId, PaymentDto paymentDto) {
        // 1. 获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "token异常");
        }

        //2.权限校验
        String draftUserId = redisCache.getHash(RedisKeyBuild.createRedisKey(RedisKeyManage.ORDER_DRAFT, draftId), "userId", String.class);
        if (!String.valueOf(userId).equals(draftUserId)) {
            throw new IsServiceException(ErrorCode.FORBIDDEN.getHttpStatusCode(), "无权操作他人订单");
        }

        // 2. 生成支付订单（获取系统订单号）
        String TransactionNo = payTransactionService.generatePayOrder(token, draftId, paymentDto);

        //3.调用支付接口
        Factory.setOptions(config);
        AlipayTradePrecreateResponse response = Factory.Payment.FaceToFace().preCreate(paymentDto.getSubject(), TransactionNo, String.valueOf(paymentDto.getAmount()));


        //4.解析结果并返回支付信息
        String httpBody = response.getHttpBody();
        JSONObject jsonObject = JSONObject.parseObject(httpBody);
        if (!jsonObject.getString("code").equals("10000")) {
            throw new IsServiceException(ErrorCode.PAYMENT_FAILED.getHttpStatusCode(), "支付失败");
        }
        String qrUrl = jsonObject.getJSONObject("alipay_trade_precreate_response").get("qr_code").toString();

        //5.生成支付二维码
        String base64Image = QrCodeUtil.generateAsBase64(qrUrl, new QrConfig(500, 500), "png");


        //6.返回支付信息
        PaymentInfoVo paymentInfoVo = new PaymentInfoVo();
        paymentInfoVo.setTransactionNo(TransactionNo);
        paymentInfoVo.setBase64Image(base64Image);
        paymentInfoVo.setDraftId(draftId);
        paymentInfoVo.setPayTime(LocalDateTime.now());
        return paymentInfoVo;

    }

    @Override
    public Boolean payNotify(HttpServletRequest request, PaymentNotifyDto paymentNotifyDto) {
        try {
            // 1. 验证支付宝签名
            Map<String, String> paramMap = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) {
                    paramMap.put(key, values[0]);
                }
            });
            boolean verifyResult = Factory.Payment.Common().verifyNotify(paramMap);
            if (!verifyResult) {
                throw new IsServiceException(ErrorCode.PAYMENT_FAILED.getHttpStatusCode(), "支付宝签名验证失败");
            }

            // 2. 获取交易信息
            String tradeNo = request.getParameter("out_trade_no");
            String msg = request.getParameter("msg");

            // 3. 更新支付订单状态
            if ("success".equals(msg)) {
                payTransactionService.updatePayStatus(tradeNo);
            }

            // 4. 自动触发生成业务订单(从缓存获取草稿)
            generateOrder(tradeNo, paymentNotifyDto);

            // 5. 异步发送通知

            return true;
        } catch (Exception e) {
            log.error("支付回调处理失败", e);
            return false;
        }
    }

    @Override
    public Boolean generateOrder(String tradeNo, PaymentNotifyDto paymentNotifyDto) {
        RedisKeyBuild draftKey = RedisKeyBuild.createRedisKey(RedisKeyManage.ORDER_DRAFT, paymentNotifyDto.getDraftId());
        if (!redisCache.hasKey(draftKey)) {
            throw new IsServiceException(ErrorCode.INVALID_PARAMETER.getHttpStatusCode(), "订单不存在");
        }
        // 1. 获取草稿信息放入订单
        Map<String, String> draft = redisCache.getAllHash(draftKey, String.class);

        PayOrder payOrder = new PayOrder();
        payOrder.setOrderId(snowflakeIdGenerator.nextId());
        payOrder.setOrderNo(paymentNotifyDto.getTransactionNo());
        payOrder.setTradeNo(tradeNo);
        payOrder.setUserId(Long.parseLong(draft.get("userId")));
        payOrder.setProductId(Long.parseLong(draft.get("workId")));
        payOrder.setSubject(draft.get("subject"));
        payOrder.setContent(draft.get("content"));
        payOrder.setOriginalAmount(new BigDecimal(draft.get("originalAmount")));
        payOrder.setDiscountAmount(new BigDecimal(draft.get("discountAmount")));
        payOrder.setTotalAmount(new BigDecimal(draft.get("totalAmount")));
        payOrder.setProductType(ProductType.getByCode(Integer.parseInt(draft.get("productType"))));
        payOrder.setTradeStatus(TradeStatus.PAID);
        payOrder.setExpireTime(null);
        payOrder.setPaidAt(paymentNotifyDto.getPayTime());
        payOrderMapper.insert(payOrder);

        // 2. 清理临时数据
        redisCache.del(draftKey);

        log.info("用户{},业务订单生成成功，订单号: {}", payOrder.getUserId(), payOrder.getOrderNo());

        return true;
    }


    /**
     * 生成草稿ID（简单有效）
     * 格式：draft_{userId}_{timestamp}
     */
    private String generateDraftId(Long userId) {
        return String.format("draft_%d_%d", userId, System.currentTimeMillis());
    }


    @Override
    public Page<OrderSimpleVo> listOrder(String token, int pageNum, int pageSize) {
        return null;
    }

    @Override
    public OrderDetailVo getOrderDetail(String token, Long orderId) {
        return null;
    }


}
