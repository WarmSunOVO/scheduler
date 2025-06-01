package com.example.intelligent_course_scheduler.payload;

import com.example.intelligent_course_scheduler.model.enums.ConstraintType;
import com.example.intelligent_course_scheduler.model.enums.TargetEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class ConstraintRuleRequestDto {
    @NotBlank(message = "约束名称不能为空")
    private String name;

    @NotBlank(message = "约束编码不能为空")
    private String constraintCode; // 例如 TEACHER_TIME_PREFERENCE, COURSE_FIXED_SCHEDULE

    @NotNull(message = "约束类型不能为空")
    private ConstraintType constraintType; // HARD, SOFT

    @NotNull(message = "目标实体类型不能为空")
    private TargetEntityType targetEntityType; // GLOBAL, TEACHER, COURSE, etc.

    private Long targetEntityId; // 可选，取决于 targetEntityType

    private Long semesterId; // 可选，约束适用的学期

    private String description;

    // 关键字段，用于存储具体参数
    private Map<String, Object> parametersJson;

    private Integer penaltyWeight; // 仅用于软约束

    private Boolean isActive = true;
}