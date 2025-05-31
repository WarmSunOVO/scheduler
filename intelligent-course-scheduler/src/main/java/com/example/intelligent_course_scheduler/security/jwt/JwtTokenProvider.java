package com.example.intelligent_course_scheduler.security.jwt;

import com.example.intelligent_course_scheduler.entity.User; // 确保这是你的用户实体类路径
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // 明确导入SignatureException
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs; // 修改为 long 类型，避免 int 溢出

    private Key key;

    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid JWT secret key: Key byte array cannot be null or empty. " +
                    "Ensure 'app.jwt.secret' in application.properties is a valid Base64 encoded string.", e);
            // 应用程序启动失败可能比静默处理更好，或者使用一个默认的、不安全的密钥并发出严重警告
            throw new RuntimeException("Invalid JWT secret key configuration. Application cannot start.", e);
        }
    }

    public String generateToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", authorities) // 将角色信息作为自定义声明
                .claim("userId", userPrincipal.getId()) // 可以添加用户ID等其他信息
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512) // 确保算法与密钥生成时一致
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()      // 使用 parserBuilder()
                .setSigningKey(key)          // 设置签名密钥
                .build()                     // 构建 JwtParser 实例
                .parseClaimsJws(token)       // 解析 JWS (JWS = JWE + JWS, 带签名的JWT)
                .getBody();                  // 获取 Claims (payload)
        return claims.getSubject();          // subject 通常是用户名
    }

    // 3. 验证 JWT Token 是否有效 (使用 parserBuilder)
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()      // 使用 parserBuilder()
                    .setSigningKey(key)  // 设置签名密钥
                    .build()             // 构建 JwtParser 实例
                    .parseClaimsJws(authToken); // 尝试解析，如果无效会抛出异常
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) { // 可能由空token或其他参数问题引起
            logger.error("JWT claims string is empty or invalid: {}", ex.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException ex) { // 明确捕获签名异常
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        }
        return false;
    }
}