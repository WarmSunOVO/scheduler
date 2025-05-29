package com.example.intelligent_course_scheduler.repository;

import com.example.intelligent_course_scheduler.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long>, JpaSpecificationExecutor<Teacher> {

    // 根据工号查询教师
    Optional<Teacher> findByTeacherIdNumber(String teacherIdNumber);

    // 根据邮箱查询教师 (如果邮箱需要唯一)
    Optional<Teacher> findByEmail(String email);

    // 检查工号是否存在 (用于校验)
    boolean existsByTeacherIdNumber(String teacherIdNumber);

    // 检查邮箱是否存在 (用于校验)
    boolean existsByEmail(String email);
}