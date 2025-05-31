package com.example.intelligent_course_scheduler.payload; // 或你的 DTO 包路径

import lombok.Data;

@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer"; // Token 类型，通常是 "Bearer"
    private Long userId; // (可选) 返回用户ID
    private String username; // (可选) 返回用户名
    // private Collection<? extends GrantedAuthority> authorities; // (可选) 返回用户权限

    public JwtAuthenticationResponse(String accessToken, Long userId, String username) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
    }

    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}