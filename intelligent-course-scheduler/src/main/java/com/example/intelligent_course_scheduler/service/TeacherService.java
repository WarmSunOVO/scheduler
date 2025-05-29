package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TeacherService {
    Teacher createTeacher(Teacher teacher);
    Optional<Teacher> getTeacherById(Long id);
    Optional<Teacher> getTeacherByTeacherIdNumber(String teacherIdNumber);
    List<Teacher> getAllTeachers();
    Page<Teacher> getTeachers(Pageable pageable); // 后续可以扩展带查询条件
    Teacher updateTeacher(Long id, Teacher teacherDetails);
    void deleteTeacher(Long id);
    boolean existsByTeacherIdNumber(String teacherIdNumber, Long excludeId); // 排除自身ID进行校验
    boolean existsByEmail(String email, Long excludeId); // 排除自身ID进行校验
}