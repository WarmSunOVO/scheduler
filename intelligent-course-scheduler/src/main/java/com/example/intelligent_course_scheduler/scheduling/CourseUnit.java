package com.example.intelligent_course_scheduler.scheduling;

import com.example.intelligent_course_scheduler.entity.TeachingTask;
import lombok.Data;
import lombok.Getter;

@Data
public class CourseUnit {
    private final String unitId; // 例如 teachingTask.getId() + "_1"
    @Getter
    private final TeachingTask teachingTask;
    private final int sessionLength;

    // 排课结果
    private TimeSlot assignedTimeSlot;
    private Long assignedRoomId;

    public CourseUnit(TeachingTask teachingTask, int unitIndex) {
        this.unitId = teachingTask.getId() + "_" + (unitIndex + 1); // unitIndex 从0开始，显示为从1开始
        this.teachingTask = teachingTask;
        this.sessionLength = teachingTask.getSessionLength(); // 确保 TeachingTask 有此 getter
    }

    public boolean isScheduled() {
        return assignedTimeSlot != null && assignedRoomId != null;
    }

    public void assign(TimeSlot timeSlot, Long roomId) {
        this.assignedTimeSlot = timeSlot;
        this.assignedRoomId = roomId;
    }

    public void unassign() {
        this.assignedTimeSlot = null;
        this.assignedRoomId = null;
    }

    @Override
    public String toString() { // 用于调试日志
        return String.format("Unit[%s] for TaskID-%d (%s) - %s",
                unitId,
                teachingTask.getId(),
                teachingTask.getCourse() != null ? teachingTask.getCourse().getName() : "N/A Course",
                isScheduled() ? "Scheduled at: " + assignedTimeSlot + " in RoomID: " + assignedRoomId : "Unscheduled");
    }
}