package com.example.intelligent_course_scheduler.scheduling;

// 用于封装排课算法的返回结果

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class ScheduleSolutionDto {
    private boolean success;
    private String message; // 例如 "排课成功" 或 "部分课程未安排"
    private List<ScheduledUnitDto> scheduledUnits;
    private List<String> conflictMessages; // 或未安排的任务列表及原因

    public ScheduleSolutionDto() {
        this.scheduledUnits = new ArrayList<>();
        this.conflictMessages = new ArrayList<>();
        this.success = false;
    }
}
