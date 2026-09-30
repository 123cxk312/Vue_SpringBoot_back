package com.example.demo.util;

import com.example.demo.config.JwtProperties;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey secretKey;
    private final long ttl;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecretKey()
                        .getBytes(StandardCharsets.UTF_8)
        );
        this.ttl = jwtProperties.getTtl();
    }

    public String generateToken(Long userId, RoleEnum role) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(ttl);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(ROLE_CLAIM, role.getCode())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new AuthException("登录状态已过期");
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AuthException("Token 无效");
        }
    }

    public Long getUserId(Claims claims) {
        String subject = claims.getSubject();

        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new AuthException("Token 中的用户信息无效");
        }
    }

    public Long getUserId(String token) {
        String subject = parseToken(token).getSubject();

        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new AuthException("Token 中的用户信息无效");
        }
    }

    public RoleEnum getRole(Claims claims) {
        String roleCode = claims.get(ROLE_CLAIM, String.class);
        RoleEnum role = RoleEnum.fromCode(roleCode);

        if (role == null) {
            throw new AuthException("Token 中的角色无效");
        }

        return role;
    }

    public RoleEnum getRole(String token) {
        String roleCode = parseToken(token).get(ROLE_CLAIM, String.class);
        RoleEnum role = RoleEnum.fromCode(roleCode);

        if (role == null) {
            throw new AuthException("Token 中的角色无效");
        }

        return role;
    }
}
