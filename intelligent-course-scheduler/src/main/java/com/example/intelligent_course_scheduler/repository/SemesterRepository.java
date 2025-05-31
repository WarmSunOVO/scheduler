package com.example.intelligent_course_scheduler.repository;

import com.example.intelligent_course_scheduler.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long>, JpaSpecificationExecutor<Semester> {
    // 检查是否存在具有相同学年和名称的学期
    boolean existsByAcademicYearAndName(String academicYear, String name);
    Optional<Semester> findByAcademicYearAndName(String academicYear, String name);
}