package com.example.intelligent_course_scheduler.payload;

import com.example.intelligent_course_scheduler.model.enums.ConstraintType;
import com.example.intelligent_course_scheduler.model.enums.TargetEntityType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ConstraintRuleResponseDto {
    private Long id;
    private String name;
    private String constraintCode;
    private ConstraintType constraintType;
    private TargetEntityType targetEntityType;
    private Long targetEntityId;
    private String targetEntityDisplay; // 可选，用于前端显示，例如教师姓名，课程名称
    private Long semesterId;
    private String semesterDisplay;
    private Boolean isActive;
    private String description;
    private Map<String, Object> parametersJson;
    private Integer penaltyWeight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}