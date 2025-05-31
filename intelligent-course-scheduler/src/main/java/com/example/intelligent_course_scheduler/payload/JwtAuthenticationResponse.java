package com.example.intelligent_course_scheduler.payload;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority; // 导入

import java.util.Collection; // 导入
import java.util.Set;       // 导入
import java.util.stream.Collectors; // 导入

@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private Set<String> roles; // <--- 修改为 Set<String> 来存储角色名

    public JwtAuthenticationResponse(String accessToken, Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        if (authorities != null) { // <--- 从 GrantedAuthority 转换
            this.roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
        }
    }

    // 保留一个简单的构造函数（如果其他地方用到）
    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}