package com.snackadmin.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 令牌提供器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成令牌
     */
    public String createToken(Long userId, String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration());

        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .claim("username", username);

        if (claims != null && !claims.isEmpty()) {
            claims.forEach(builder::claim);
        }

        return builder.signWith(getKey()).compact();
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserId(String token) {
        String subject = parseClaims(token).getSubject();
        return Long.valueOf(subject);
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT验证失败: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
