package com.example.intelligent_course_scheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_unavailabilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherUnavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher; // 假设您的 Teacher 实体在此包或可被导入

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(nullable = false)
    private Integer dayOfWeek;

    @Column(nullable = false)
    private Integer startPeriod;

    @Column(nullable = false)
    private Integer endPeriod;

    private String reason;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @Column(updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}