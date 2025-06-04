package com.example.intelligent_course_scheduler.scheduling;

// 用于Controller接收排课请求

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchedulingRequestDto {
    @NotNull(message = "学期ID不能为空")
    private Long semesterId;
    // 未来可以添加其他参数，如排课策略、优先级等
}