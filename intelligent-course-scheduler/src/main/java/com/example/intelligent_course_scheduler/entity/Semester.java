package com.example.intelligent_course_scheduler.entity; // 确认包名与您项目一致

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // 保留您原有的 @Builder
import org.hibernate.annotations.CreationTimestamp; // 保留您原有的 Hibernate 注解
import org.hibernate.annotations.UpdateTimestamp;   // 保留您原有的 Hibernate 注解

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "semester", uniqueConstraints = {
        // 保留您原有的唯一约束
        @UniqueConstraint(columnNames = {"academic_year", "name"}, name = "uk_semester_year_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // 保留您原有的 @Builder
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_year", nullable = false, length = 50)
    private String academicYear;

    @Column(nullable = false, length = 50) // 保持您原有的 length = 50
    private String name;

    @Column(name = "start_date") // 保留您原有的 nullable = true (因为没有 nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date") // 保留您原有的 nullable = true
    private LocalDate endDate;

    @Column(name = "is_current", columnDefinition = "BOOLEAN DEFAULT FALSE") // 明确 columnDefinition
    private Boolean isCurrent = false;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String remarks; // 保留您原有的 remarks 字段

    // ------------------- 新增字段 -------------------
    @Column(name = "total_weeks") // 建议为新字段也明确列名
    private Integer totalWeeks;   // 学期总周数

    @Column(name = "periods_per_day")
    private Integer periodsPerDay; // 每日总节次数

    @Column(name = "days_per_week")
    private Integer daysPerWeek;   // 每周上课天数 (例如 5 或 7)
    // -------------------------------------------------

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}