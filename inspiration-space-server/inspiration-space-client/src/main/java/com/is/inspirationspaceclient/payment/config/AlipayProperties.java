package com.is.inspirationspaceclient.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "alipay.easy")
public class AlipayProperties {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 请求协议
     */
    private String protocol;

    /**
     * 请求网关
     */
    private String gatewayHost;

    /**
     * 签名方式
     */
    private String signType;

    /**
     * 应用私钥
     */
    private String merchantPrivateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 异步回调地址
     */
    private String notifyUrl;

}
