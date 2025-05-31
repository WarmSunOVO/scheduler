package com.example.intelligent_course_scheduler.payload;
//用于返回用户信息给前端 (不包含密码)
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.intelligent_course_scheduler.entity.User; // 你的User实体
import com.example.intelligent_course_scheduler.entity.Role; // 你的Role实体

@Data
@Builder //方便构建对象
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private boolean enabled;
    private boolean locked;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 静态工厂方法，从User实体转换
    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .enabled(user.isEnabled())
                .locked(user.isLocked())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}