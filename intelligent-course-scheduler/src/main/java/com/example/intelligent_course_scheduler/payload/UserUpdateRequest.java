package com.example.intelligent_course_scheduler.payload;
//用于更新用户信息
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateRequest {
    // 用户名通常不允许直接修改，或通过特定流程
    // private String username;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 100)
    private String fullName;

    private Boolean enabled;
    private Boolean locked; // 是否锁定账户
    // 密码更新通常有专门的接口

    private Set<String> roles; // 更新后的角色名称集合
}