package com.example.intelligent_course_scheduler.payload;

import com.example.intelligent_course_scheduler.entity.TeachingTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingTaskDto {
    private Long id;
    private Long semesterId;
    private String semesterDisplay; // 例如 "2024-2025 第一学期"
    private Long courseId;
    private String courseName; // 课程名称
    private String courseCode; // 课程代码
    private Long teacherId;
    private String teacherName; // 教师姓名
    private String targetGroup;
    private Integer hoursPerWeek;
    private Integer sessionsPerWeek;
    private Integer sessionLength;
    private String requiredRoomType;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeachingTaskDto fromEntity(TeachingTask task) {
        if (task == null) return null;
        return TeachingTaskDto.builder()
                .id(task.getId())
                .semesterId(task.getSemester().getId())
                .semesterDisplay(task.getSemester().getAcademicYear() + " " + task.getSemester().getName())
                .courseId(task.getCourse().getId())
                .courseName(task.getCourse().getName())
                .courseCode(task.getCourse().getCourseCode())
                .teacherId(task.getTeacher().getId())
                .teacherName(task.getTeacher().getName())
                .targetGroup(task.getTargetGroup())
                .hoursPerWeek(task.getHoursPerWeek())
                .sessionsPerWeek(task.getSessionsPerWeek())
                .sessionLength(task.getSessionLength())
                .requiredRoomType(task.getRequiredRoomType())
                .remarks(task.getRemarks())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}