package com.example.intelligent_course_scheduler.payload; // 或 com.example.intelligent_course_scheduler.dto

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeacherUnavailabilityResponseDto {
    private Long id;
    private Long teacherId;
    private String teacherName; // 可以从 Teacher 实体获取
    private Long semesterId;
    private String semesterDisplay; // 例如 "2023-2024 第一学期"
    private Integer dayOfWeek;
    private Integer startPeriod;
    private Integer endPeriod;
    private String reason;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}