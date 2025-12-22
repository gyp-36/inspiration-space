
package com.is.inspirationspacecommon.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

/**
 * JWT工具类
 *
 * 功能：
 * 1. 生成包含过期时间、用户ID和用户名的JWT令牌
 * 2. 验证JWT令牌的有效性
 * 3. 从令牌中提取用户信息
 */
@Component
public class JwtUtil {

    // 密钥
    private static final String SECRET_KEY ="2CVA14hqfNsVQgA36v/tMFNXvDV90ERK2Vzm2G+17X0=";

    private static final long DEFAULT_EXPIRATION_TIME =  3* 60 * 60* 1000 ;


    /**
     * 获取签名密钥
     */
    private static Key getSigningKey() {
       return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * 生成JWT令牌（使用默认过期时间）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT令牌字符串
     */
    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, DEFAULT_EXPIRATION_TIME);
    }

    /**
     * 生成JWT令牌（自定义过期时间）
     *
     * @param userId          用户ID
     * @param username        用户名
     * @param expirationTime  过期时间（毫秒）
     * @return JWT令牌字符串
     */
    public static String generateToken(Long userId, String username, long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        String jwtId = UUID.randomUUID().toString();
        claims.put("jti", jwtId);

        claims.put("rnd", RandomStringUtils.randomAlphanumeric(8));

        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            if (token != null) {
                token = token.trim();
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
            }
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从JWT令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从JWT令牌中获取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public static Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 检查JWT令牌是否已过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 从JWT令牌中获取所有声明信息
     */
    private static Claims getClaimsFromToken(String token) {
        // 对token进行预处理，去除首尾空格并移除"Bearer "前缀（如果存在）
        if (token != null) {
            token = token.trim();
            if (token.startsWith("Bearer ")) {
                token = token.substring(7); // "Bearer " 长度为7
            }
        }
        
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}
