package com.example.intelligent_course_scheduler.repository;

import com.example.intelligent_course_scheduler.entity.TeacherUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeacherUnavailabilityRepository extends JpaRepository<TeacherUnavailability, Long> {
    List<TeacherUnavailability> findByTeacherIdAndSemesterIdAndIsActiveTrue(Long teacherId, Long semesterId);
    List<TeacherUnavailability> findBySemesterIdAndIsActiveTrue(Long semesterId);
    // 如果需要，可以添加按 teacherId 查询所有学期不可用的方法
    List<TeacherUnavailability> findByTeacherIdAndIsActiveTrue(Long teacherId);
}