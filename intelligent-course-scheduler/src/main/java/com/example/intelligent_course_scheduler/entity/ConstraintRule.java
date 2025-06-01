package com.example.intelligent_course_scheduler.entity;

import com.example.intelligent_course_scheduler.model.enums.ConstraintType; // 确保 import 正确
import com.example.intelligent_course_scheduler.model.enums.TargetEntityType; // 确保 import 正确
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "constraint_rules", uniqueConstraints = {
        @UniqueConstraint(columnNames = "constraint_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConstraintRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 100)
    private String constraintCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('HARD', 'SOFT')")
    private ConstraintType constraintType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('GLOBAL', 'TEACHER', 'CLASS_GROUP', 'COURSE', 'ROOM', 'TEACHING_TASK')")
    private TargetEntityType targetEntityType;

    private Long targetEntityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> parametersJson;

    private Integer penaltyWeight;

    @Column(updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}