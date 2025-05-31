package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.entity.User;
import com.example.intelligent_course_scheduler.payload.JwtAuthenticationResponse;
import com.example.intelligent_course_scheduler.payload.LoginRequest;
import com.example.intelligent_course_scheduler.security.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import org.slf4j.Logger; // 添加 Logger
import org.slf4j.LoggerFactory; // 添加 LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // 添加 HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // 明确捕获
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException; // 捕获通用的 AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap; // 用于构建错误响应体
import java.util.Map;     // 用于构建错误响应体
import java.time.LocalDateTime; // 用于时间戳

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class); // 添加 Logger

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Attempting authentication for user: {}", loginRequest.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User userPrincipal = (User) authentication.getPrincipal();
            String jwt = tokenProvider.generateToken(authentication);

            logger.info("User {} authenticated successfully. JWT generated.", loginRequest.getUsername());
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, userPrincipal.getId(), userPrincipal.getUsername()));

        } catch (BadCredentialsException e) { // 专门捕获 BadCredentialsException
            logger.warn("Authentication failed for user {}: Invalid credentials", loginRequest.getUsername(), e);
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now().toString());
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "Unauthorized");
            body.put("message", "用户名或密码错误"); // 更友好的中文提示
            body.put("path", "/api/auth/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        } catch (AuthenticationException e) { // 捕获其他认证相关的异常 (例如用户被锁定，账户过期等)
            logger.warn("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now().toString());
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "Unauthorized");
            body.put("message", "认证失败: " + e.getMessage());
            body.put("path", "/api/auth/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }
}