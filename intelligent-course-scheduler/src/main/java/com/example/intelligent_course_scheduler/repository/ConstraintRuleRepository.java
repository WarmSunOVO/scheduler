package com.example.intelligent_course_scheduler.repository;

import com.example.intelligent_course_scheduler.entity.ConstraintRule;
import com.example.intelligent_course_scheduler.model.enums.ConstraintType; // 确保 import
import com.example.intelligent_course_scheduler.model.enums.TargetEntityType; // 确保 import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 添加分页和动态查询支持
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConstraintRuleRepository extends JpaRepository<ConstraintRule, Long>, JpaSpecificationExecutor<ConstraintRule> { // 添加 JpaSpecificationExecutor
    List<ConstraintRule> findBySemesterIdAndIsActiveTrue(Long semesterId);
    // 查找全局且激活的约束 (semesterId is NULL and isActive = true)
    List<ConstraintRule> findBySemesterIsNullAndIsActiveTrue();
    List<ConstraintRule> findByConstraintCodeAndIsActiveTrue(String constraintCode);
    List<ConstraintRule> findByTargetEntityTypeAndTargetEntityIdAndSemesterIdAndIsActiveTrue(
            TargetEntityType entityType,
            Long entityId,
            Long semesterId
    );
    List<ConstraintRule> findByConstraintTypeAndIsActiveTrue(ConstraintType constraintType);
}