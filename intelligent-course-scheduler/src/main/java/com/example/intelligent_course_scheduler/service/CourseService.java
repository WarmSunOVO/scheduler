package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.payload.CourseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CourseService {
    CourseDto createCourse(CourseDto courseDto);
    Optional<CourseDto> getCourseById(Long id);
    Page<CourseDto> getAllCourses(Pageable pageable, String query); // query 可以用于搜索课程代码或名称
    List<CourseDto> getAllCoursesList(); // 不分页
    CourseDto updateCourse(Long id, CourseDto courseDto);
    void deleteCourse(Long id);
}