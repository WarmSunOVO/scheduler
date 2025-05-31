package com.example.intelligent_course_scheduler.payload;
//用于创建新用户
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class UserCreateRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100) // 密码长度要求
    private String password;

    @Email
    @Size(max = 100)
    private String email; // 可选

    @Size(max = 100)
    private String fullName; // 可选

    private Boolean enabled; // 可选，默认为true

    private Set<String> roles; // 角色名称集合, e.g., ["ROLE_ADMIN", "ROLE_TEACHER"]
}