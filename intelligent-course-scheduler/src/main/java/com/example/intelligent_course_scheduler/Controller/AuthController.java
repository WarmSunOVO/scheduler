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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.intelligent_course_scheduler.payload.UserResponse; // 使用我们已有的UserResponse DTO



import java.util.HashMap; // 用于构建错误响应体
import java.util.Map;     // 用于构建错误响应体
import java.time.LocalDateTime; // 用于时间戳
import java.util.stream.Collectors;
import java.util.Collection; // 确保导入

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

            // 获取用户的权限 (GrantedAuthority 集合)
            Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

            logger.info("User {} authenticated successfully. JWT generated. Roles: {}", loginRequest.getUsername(), authorities);
            // 将权限信息也传递给 JwtAuthenticationResponse
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, userPrincipal.getId(), userPrincipal.getUsername(), authorities));


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

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // 确保用户已通过JWT认证
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // 这种情况理论上在启用了 isAuthenticated() 后不应发生，但作为防御性编程
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未认证或会话无效");
        }

        // principal 应该是我们的 User 实体，因为它是在 JwtAuthenticationFilter 中设置的
        if (authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            // 使用 UserResponse DTO 返回选择性的用户信息，不包括密码
            UserResponse userResponse = UserResponse.fromUser(currentUser);
            return ResponseEntity.ok(userResponse);
        } else {
            // 如果 principal 不是 User 类型，可能是一个字符串或其他，这通常意味着认证流程有问题
            logger.warn("Current user principal is not an instance of User: {}", authentication.getPrincipal().getClass().getName());
            // 可以返回一个通用的用户信息，或者一个错误
            // 为了安全，不直接返回 principal，因为它可能是敏感的
            Map<String, Object> principalInfo = new HashMap<>();
            principalInfo.put("username", authentication.getName()); // 尝试获取用户名
            principalInfo.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            return ResponseEntity.ok(principalInfo);
        }
    }

}