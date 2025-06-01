package com.example.intelligent_course_scheduler.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class RoomUnavailabilityRequestDto {

    @NotNull(message = "教室ID不能为空")
    private Long roomId;

    @NotNull(message = "学期ID不能为空")
    private Long semesterId;

    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几必须在1-7之间")
    @Max(value = 7, message = "星期几必须在1-7之间")
    private Integer dayOfWeek; // 1=周一, ..., 7=周日

    @NotNull(message = "开始节次不能为空")
    @Min(value = 1, message = "节次必须大于0")
    private Integer startPeriod;

    @NotNull(message = "结束节次不能为空")
    @Min(value = 1, message = "节次必须大于0")
    // 可以添加一个自定义校验器确保 endPeriod >= startPeriod
    private Integer endPeriod;

    private String reason;

    private Boolean isActive = true; // 默认为激活
}