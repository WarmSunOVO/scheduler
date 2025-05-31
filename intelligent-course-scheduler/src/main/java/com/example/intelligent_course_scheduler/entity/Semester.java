package com.example.intelligent_course_scheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate; // 使用 LocalDate 对应 DATE 类型
import java.time.LocalDateTime;

@Entity
@Table(name = "semester", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"academic_year", "name"}, name = "uk_semester_year_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_year", nullable = false, length = 50)
    private String academicYear;

    @Column(nullable = false, length = 50)
    private String name; // e.g., "第一学期", "第二学期"

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    private Boolean isCurrent = false; // 默认为 false

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