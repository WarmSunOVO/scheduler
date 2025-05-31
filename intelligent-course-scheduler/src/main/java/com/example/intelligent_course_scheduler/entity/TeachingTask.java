package com.example.intelligent_course_scheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "teaching_task", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"semester_id", "course_id", "target_group"}, name = "uk_teaching_task_semester_course_group")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 多对一，一个学期可以有多个教学任务
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "target_group", nullable = false, length = 255)
    private String targetGroup; // 上课对象/班级描述

    @Column(name = "hours_per_week", nullable = false)
    private Integer hoursPerWeek; // 周学时

    @Column(name = "sessions_per_week", nullable = false)
    private Integer sessionsPerWeek; // 每周上课次数 (例如，周学时4，每周上2次，每次2节)

    @Column(name = "session_length", nullable = false)
    private Integer sessionLength; // 每次上课的连堂节数 (例如 1, 2, 3)

    @Column(name = "required_room_type", length = 50)
    private String requiredRoomType; // 优先考虑的教室类型

    @Lob
    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}