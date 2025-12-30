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
import com.is.inspirationspaceclient.payment.model.entity.PayTransaction;
import com.is.inspirationspaceclient.payment.model.entity.enums.ProductType;
import com.is.inspirationspaceclient.payment.model.entity.enums.TradeStatus;
import com.is.inspirationspaceclient.payment.model.vo.*;
import com.is.inspirationspaceclient.user.rabbitmq.UserMessageProducer;
import com.is.inspirationspaceclient.work.mapper.WorkInfoMapper;
import com.is.inspirationspaceclient.work.mapper.WorkStatsMapper;
import com.is.inspirationspaceclient.work.model.entity.WorkInfo;
import com.is.inspirationspaceclient.work.model.entity.WorkStats;
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

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    private final WorkStatsMapper workStatsMapper;

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    private final RedisCache redisCache;

    private final Config config;

    private final PayTransactionService payTransactionService;
    private final PayOrderMapper payOrderMapper;
    private final com.is.inspirationspacecommon.config.StorageService storageService;
    private final ApplicationContext applicationContext;
    private final UserMessageProducer userMessageProducer;


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
        
        // 处理封面图
        String coverUrl = workInfo.getCoverUrl();
        if (coverUrl != null && !coverUrl.isEmpty() && !coverUrl.startsWith("http")) {
            try {
                coverUrl = storageService.getPreSignedUrl("work", coverUrl, 1, java.util.concurrent.TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("生成订单预览封面URL失败: {}", coverUrl);
            }
        }
        orderPreviewVo.setCoverUrl(coverUrl);
        
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
            draftData.put("productType", String.valueOf(ProductType.PRODUCT.getCode()));
            draftData.put("tradeStatus", String.valueOf(TradeStatus.CREATED.getCode()));
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
        try {
            Factory.setOptions(config);
            log.info("开始调用支付宝预下单接口，订单号：{}，金额：{}", TransactionNo, paymentDto.getAmount());
            AlipayTradePrecreateResponse response = Factory.Payment.FaceToFace()
                    .preCreate(paymentDto.getSubject(), TransactionNo, String.valueOf(paymentDto.getAmount()));

            //4.解析结果并返回支付信息
            String httpBody = response.getHttpBody();
            log.debug("支付宝响应内容：{}", httpBody);
            JSONObject jsonObject = JSONObject.parseObject(httpBody);
            JSONObject precreateResponse = jsonObject.getJSONObject("alipay_trade_precreate_response");
            
            if (precreateResponse == null || !"10000".equals(precreateResponse.getString("code"))) {
                String subMsg = precreateResponse != null ? precreateResponse.getString("sub_msg") : "未知错误";
                log.error("支付宝预下单失败：{}", subMsg);
                throw new IsServiceException(ErrorCode.PAYMENT_FAILED.getHttpStatusCode(), "支付失败：" + subMsg);
            }
            
            String qrUrl = precreateResponse.getString("qr_code");
            if (qrUrl == null) {
                log.error("支付宝响应未包含二维码地址");
                throw new IsServiceException(ErrorCode.PAYMENT_FAILED.getHttpStatusCode(), "获取支付二维码失败");
            }

            //5.生成支付二维码
            String base64Image = QrCodeUtil.generateAsBase64(qrUrl, new QrConfig(500, 500), "png");

            //6.返回支付信息
            PaymentInfoVo paymentInfoVo = new PaymentInfoVo();
            paymentInfoVo.setTransactionNo(TransactionNo);
            paymentInfoVo.setBase64Image(base64Image);
            paymentInfoVo.setDraftId(draftId);
            paymentInfoVo.setPayTime(LocalDateTime.now());
            return paymentInfoVo;
            
        } catch (Exception e) {
            log.error("支付宝支付异常", e);
            if (e instanceof IsServiceException) {
                throw e;
            }
            throw new IsServiceException(ErrorCode.PAYMENT_FAILED.getHttpStatusCode(), "支付宝服务异常，请稍后再试");
        }

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
            String outTradeNo = request.getParameter("out_trade_no");
            String tradeNo = request.getParameter("trade_no");
            String tradeStatus = request.getParameter("trade_status");

            if (outTradeNo == null || tradeNo == null) {
                log.error("支付宝回调缺少必要参数：outTradeNo={}, tradeNo={}", outTradeNo, tradeNo);
                return false;
            }

            log.info("收到支付宝回调通知：outTradeNo={}, tradeNo={}, tradeStatus={}", outTradeNo, tradeNo, tradeStatus);

            // 3. 更新支付订单状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                payTransactionService.updatePayStatus(outTradeNo);
                
                // 4. 从支付交易记录中获取 draftId
                PayTransaction payTransaction = transactionMapper.selectByTransactionNo(outTradeNo);
                if (payTransaction == null) {
                    log.error("支付交易记录不存在：{}", outTradeNo);
                    return false;
                }
                
                // 5. 自动触发生成业务订单
                if (paymentNotifyDto == null) {
                    paymentNotifyDto = new PaymentNotifyDto();
                }
                paymentNotifyDto.setTransactionNo(outTradeNo);
                paymentNotifyDto.setDraftId(payTransaction.getDraftId());
                paymentNotifyDto.setPayTime(LocalDateTime.now());
                
                // 使用代理对象调用，确保事务生效
                PayOrderService proxy = applicationContext.getBean(PayOrderService.class);
                Boolean generateResult = proxy.generateOrder(tradeNo, paymentNotifyDto);
                if (!generateResult) {
                    log.error("自动触发生成业务订单失败，单号：{}", outTradeNo);
                    return false;
                }
                log.info("业务订单自动生成成功，单号：{}", outTradeNo);
            }

            return true;
        } catch (Exception e) {
            log.error("支付回调处理过程中发生异常", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateOrder(String tradeNo, PaymentNotifyDto paymentNotifyDto) {
        log.info("开始为交易 {} 生成业务订单, 草稿ID: {}", paymentNotifyDto.getTransactionNo(), paymentNotifyDto.getDraftId());

        // 幂等性检查：防止重复生成业务订单
        PayOrder existingOrder = payOrderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PayOrder>()
                .eq("trade_no", tradeNo)
                .or()
                .eq("order_no", paymentNotifyDto.getTransactionNo()));
        if (existingOrder != null) {
            log.warn("业务订单已存在，跳过生成。订单号: {}, 交易号: {}", existingOrder.getOrderNo(), tradeNo);
            return true;
        }

        RedisKeyBuild draftKey = RedisKeyBuild.createRedisKey(RedisKeyManage.ORDER_DRAFT, paymentNotifyDto.getDraftId());
        if (!redisCache.hasKey(draftKey)) {
            log.error("生成业务订单失败：Redis中不存在草稿ID {}", paymentNotifyDto.getDraftId());
            throw new IsServiceException(ErrorCode.INVALID_PARAMETER.getHttpStatusCode(), "订单草稿已过期或不存在");
        }
        
        // 1. 获取草稿信息
        Map<String, String> draft = redisCache.getAllHash(draftKey, String.class);
        log.info("获取到订单草稿数据: {}", draft);

        try {
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
            
            // 确保从草稿中映射所有关键业务字段
            payOrder.setSubject(draft.getOrDefault("subject", payOrder.getSubject()));
            payOrder.setContent(draft.getOrDefault("content", payOrder.getContent()));
            
            // 鲁棒性处理枚举转换
            String productTypeStr = draft.get("productType");
            ProductType productType = null;
            try {
                productType = ProductType.getByCode(Integer.parseInt(productTypeStr));
            } catch (Exception e) {
                log.warn("解析 productType 失败: {}, 尝试通过名称查找", productTypeStr);
                for (ProductType pt : ProductType.values()) {
                    if (pt.name().equals(productTypeStr) || pt.getDesc().equals(productTypeStr)) {
                        productType = pt;
                        break;
                    }
                }
            }
            payOrder.setProductType(productType != null ? productType : ProductType.PRODUCT);
            
            payOrder.setTradeStatus(TradeStatus.PAY);
            payOrder.setExpireTime(null);
            payOrder.setPAYAt(paymentNotifyDto.getPayTime());
            
            log.info("正在插入业务订单记录: {}", payOrder.getOrderNo());
            int rows = payOrderMapper.insert(payOrder);
            if (rows <= 0) {
                log.error("业务订单记录插入失败，影响行数为0, 订单号: {}", payOrder.getOrderNo());
                throw new IsServiceException(ErrorCode.PAYMENT_FAILED.getHttpStatusCode(), "业务订单插入失败");
            }
            log.info("业务订单记录插入成功，ID: {}, 订单号: {}", payOrder.getOrderId(), payOrder.getOrderNo());

            // 2. 根据产品类型更新相关业务状态
            if (payOrder.getProductType() == ProductType.PRODUCT) {
                // 更新作品的销量和收入
                Long workId = payOrder.getProductId();
                log.info("正在更新作品 {} 的销量和收入", workId);
                WorkInfo workInfo = workInfoMapper.selectById(workId);
                if (workInfo != null) {
                    workInfo.setTotalSales(workInfo.getTotalSales() != null ? workInfo.getTotalSales() + 1 : 1);
                    workInfo.setTotalRevenue(workInfo.getTotalRevenue() != null ? 
                        workInfo.getTotalRevenue().add(payOrder.getTotalAmount()) : payOrder.getTotalAmount());
                    workInfoMapper.updateById(workInfo);
                
                    // 更新统计表
                    WorkStats workStats = workStatsMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WorkStats>()
                        .eq("work_id", workId));
                    if (workStats != null) {
                        workStats.setPurchaseCount(workStats.getPurchaseCount() != null ? 
                            workStats.getPurchaseCount() + 1 : 1);
                        workStatsMapper.updateById(workStats);
                    }

                    // 发送消息给作者
                    userMessageProducer.sendFastMessage("notification", 
                        String.format("您的作品《%s》已被购买，收入 ￥%s", workInfo.getTitle(), payOrder.getTotalAmount()));
                }
            } else if (payOrder.getProductType() == ProductType.MEMBER) {
                // TODO: 处理会员逻辑，更新用户会员状态
                log.info("处理会员购买逻辑，用户ID: {}", payOrder.getUserId());
                userMessageProducer.sendFastMessage("notification", "恭喜您成为本站会员！");
            }

            // 发送通知给买家
            userMessageProducer.sendFastMessage("notification", 
                String.format("您已成功购买《%s》，订单号: %s", payOrder.getSubject(), payOrder.getOrderNo()));

            // 3. 清理临时数据
            redisCache.del(draftKey);

            log.info("用户 {}, 业务订单生成成功，订单号: {}", payOrder.getUserId(), payOrder.getOrderNo());
            return true;
            
        } catch (Exception e) {
            log.error("生成业务订单过程中发生异常", e);
            throw new IsServiceException(ErrorCode.PAYMENT_FAILED.getHttpStatusCode(), "生成业务订单失败");
        }
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
