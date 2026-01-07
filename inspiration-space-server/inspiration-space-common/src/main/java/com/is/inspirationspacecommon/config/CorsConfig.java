package com.is.inspirationspacecommon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource customcorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的源，使用 patterns 以支持 credentials
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://8.163.13.117:*",
                "https://8.163.13.117:*"
        ));
        // 允许的方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 允许的头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许携带凭证（如 Cookie, Authorization 头）
        configuration.setAllowCredentials(true);
        // 预检请求的有效期（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}