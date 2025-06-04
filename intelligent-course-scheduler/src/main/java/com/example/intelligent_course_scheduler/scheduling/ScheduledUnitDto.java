package com.example.intelligent_course_scheduler.scheduling;

// 用于表示算法输出的已排课程单元

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledUnitDto {
    private Long teachingTaskId;
    private String courseName;
    private String teacherName;
    private String targetGroup;
    private int dayOfWeek;
    private int startPeriod;
    private int endPeriod;
    private Long roomId;
    private String roomNumber;
    // 可以根据需要添加更多显示信息
}
