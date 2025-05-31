package com.example.intelligent_course_scheduler.repository;

import com.example.intelligent_course_scheduler.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    boolean existsByCourseCode(String courseCode);
    Optional<Course> findByCourseCode(String courseCode);
}