package thedayoftoday.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final long accessExpirationTime;
    private final long refreshExpirationTime;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret,
                   @Value("${spring.jwt.expiration_time}") long accessExpirationTime,
                   @Value("${spring.jwt.refresh-expiration-time}") long refreshExpirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));  // SecretKey 생성
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public String getUsername(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String getCategory(String token) {
        return parseClaims(token).get("category", String.class);
    }

    public Boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    public String createAccessToken(String category, String email, String role, Long userId) {
        log.info("사용자의 email {}, role: {}, expires in {} ms", email, role, accessExpirationTime);
        return createJwt("access", email, role, userId, accessExpirationTime);
    }

    public String createRefreshToken(String category, String email, String role, Long userId) {
        log.info("사용자의 email {}, role: {}, expires in {} ms", email, role, refreshExpirationTime);
        return createJwt("refresh", email, role, userId, refreshExpirationTime);
    }

    public String createJwt(String category, String email, String role, Long userId, long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("email", email)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))  // 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)  // 검증할 때 사용
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}