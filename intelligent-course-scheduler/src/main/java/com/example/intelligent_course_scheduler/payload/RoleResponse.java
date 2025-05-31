package com.example.intelligent_course_scheduler.payload;
//用于返回角色信息
import com.example.intelligent_course_scheduler.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    private Long id;
    private String name;
    private String description;

    public static RoleResponse fromRole(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}