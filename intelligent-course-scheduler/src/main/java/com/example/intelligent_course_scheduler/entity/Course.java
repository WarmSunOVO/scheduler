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
@Table(name = "course")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", nullable = false, unique = true, length = 50)
    private String courseCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Double credits;

    @Column(name = "hours_per_week")
    private Integer hoursPerWeek;

    @Column(name = "total_hours")
    private Integer totalHours;

    @Column(length = 50)
    private String type; // e.g., "理论课", "实验课"

    @Column(name = "required_room_type", length = 50)
    private String requiredRoomType;

    @Column(name = "department_name", length = 100) // 存储院系名称
    private String departmentName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20, columnDefinition = "VARCHAR(20) DEFAULT '激活'")
    private String status = "激活";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}