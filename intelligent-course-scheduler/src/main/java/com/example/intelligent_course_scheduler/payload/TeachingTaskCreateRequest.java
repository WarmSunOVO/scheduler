package com.example.intelligent_course_scheduler.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeachingTaskCreateRequest {
    @NotNull(message = "学期ID不能为空")
    private Long semesterId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotBlank(message = "上课对象/班级不能为空")
    @Size(max = 255)
    private String targetGroup;

    @NotNull(message = "周学时不能为空")
    @Min(value = 1, message = "周学时必须大于0")
    private Integer hoursPerWeek;

    @NotNull(message = "每周上课次数不能为空")
    @Min(value = 1, message = "每周上课次数必须大于0")
    private Integer sessionsPerWeek;

    @NotNull(message = "每次连堂节数不能为空")
    @Min(value = 1, message = "每次连堂节数必须大于0")
    private Integer sessionLength;

    @Size(max = 50)
    private String requiredRoomType;

    private String remarks;
}