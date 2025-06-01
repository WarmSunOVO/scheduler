package com.example.intelligent_course_scheduler.payload;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClassGroupUnavailabilityResponseDto {
    private Long id;
    private String targetGroup;
    private Long semesterId;
    private String semesterDisplay;
    private Integer dayOfWeek;
    private Integer startPeriod;
    private Integer endPeriod;
    private String reason;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}