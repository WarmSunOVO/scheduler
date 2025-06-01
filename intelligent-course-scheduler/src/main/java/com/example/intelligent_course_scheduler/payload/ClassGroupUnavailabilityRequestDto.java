package com.example.intelligent_course_scheduler.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class ClassGroupUnavailabilityRequestDto {

    @NotBlank(message = "教学对象标识不能为空")
    private String targetGroup;

    @NotNull(message = "学期ID不能为空")
    private Long semesterId;

    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几必须在1-7之间")
    @Max(value = 7, message = "星期几必须在1-7之间")
    private Integer dayOfWeek;

    @NotNull(message = "开始节次不能为空")
    @Min(value = 1, message = "节次必须大于0")
    private Integer startPeriod;

    @NotNull(message = "结束节次不能为空")
    @Min(value = 1, message = "节次必须大于0")
    private Integer endPeriod;

    private String reason;
    private Boolean isActive = true;
}