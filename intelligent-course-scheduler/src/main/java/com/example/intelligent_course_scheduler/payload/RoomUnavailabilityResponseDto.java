package com.example.intelligent_course_scheduler.payload;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoomUnavailabilityResponseDto {
    private Long id;
    private Long roomId;
    private String roomName; // 可以从 Classroom 实体获取，例如教室名称
    private String roomNumber; // 或者教室编号，取决于您的 Classroom 实体设计
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