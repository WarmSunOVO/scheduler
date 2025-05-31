package com.example.intelligent_course_scheduler.payload; // 或你的 DTO 包路径

import jakarta.validation.constraints.NotBlank; // 用于参数校验
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}