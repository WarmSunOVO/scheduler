package com.example.intelligent_course_scheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher")
@Data
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id_number", nullable = false, unique = true, length = 50)
    private String teacherIdNumber; // 教师工号

    @Column(nullable = false, length = 100)
    private String name; // 姓名

    @Column(length = 10)
    private String gender; // 性别

    @Column(length = 50)
    private String title; // 职称

    @Column(length = 100)
    private String department; // 院系

    @Column(length = 100, unique = true)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Lob
    @Column(name = "teachable_courses", columnDefinition = "TEXT")
    private String teachableCourses; // 可授课程 (JSON或逗号分隔)

    @Lob
    @Column(name = "unavailable_slots", columnDefinition = "TEXT")
    private String unavailableSlots; // 不便排课时间 (JSON或文本描述)

    @Column(length = 20, columnDefinition = "VARCHAR(20) DEFAULT '在职'")
    private String status = "在职";

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