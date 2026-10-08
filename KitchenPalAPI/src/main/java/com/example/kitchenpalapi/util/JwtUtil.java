package com.example.kitchenpalapi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发与校验 Bearer Token（jjwt 0.12.x，HS256）。
 * token 含 sub=用户ID、phone、iat、exp。
 * 登录/注册成功后由 UserController 调用 generateToken 下发；
 * JwtAuthFilter 在受保护路径上调用 parseUserId 校验。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:kitchenpal-default-secret-key-change-me-in-prod-1234567890}")
    private String secret;

    @Value("${jwt.expiration:604800000}") // 默认 7 天（毫秒）
    private long expiration;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String phone) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    /** 校验并返回 userId；非法/过期返回 null */
    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validate(String token) {
        return parseUserId(token) != null;
    }
}
