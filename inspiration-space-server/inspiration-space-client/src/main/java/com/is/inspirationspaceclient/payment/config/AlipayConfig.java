package com.is.inspirationspaceclient.payment.config;

import com.alipay.easysdk.kernel.Config;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AlipayConfig {

    @Bean
    public Config config(AlipayProperties alipayProperties) {
        Config config = new Config();
        config.appId=alipayProperties.getAppId();
        config.protocol=alipayProperties.getProtocol();
        config.gatewayHost=alipayProperties.getGatewayHost();
        config.signType=alipayProperties.getSignType();
        config.alipayPublicKey=alipayProperties.getAlipayPublicKey();
        config.merchantPrivateKey=alipayProperties.getMerchantPrivateKey();
        config.notifyUrl=alipayProperties.getNotifyUrl();
        return config;
    }
}
