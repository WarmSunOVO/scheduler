package com.example.intelligent_course_scheduler.repository;

import com.example.intelligent_course_scheduler.entity.TeachingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeachingTaskRepository extends JpaRepository<TeachingTask, Long>, JpaSpecificationExecutor<TeachingTask> {
    // 根据学期ID和班级描述查询任务
    List<TeachingTask> findBySemesterIdAndTargetGroup(Long semesterId, String targetGroup);
    // 检查是否存在相同的任务（学期、课程、班级）
    boolean existsBySemesterIdAndCourseIdAndTargetGroup(Long semesterId, Long courseId, String targetGroup);
    // (可选) 检查是否存在相同的任务，但在更新时排除自身
    boolean existsBySemesterIdAndCourseIdAndTargetGroupAndIdNot(Long semesterId, Long courseId, String targetGroup, Long id);

    List<TeachingTask> findBySemesterId(Long semesterId);
}