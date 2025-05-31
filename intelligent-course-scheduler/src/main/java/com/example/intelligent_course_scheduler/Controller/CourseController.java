package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.payload.CourseDto;
import com.example.intelligent_course_scheduler.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils; // 确保导入
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList; // 确保导入
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')") // 管理员或教师可创建课程
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto createdCourse = courseService.createCourse(courseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // 任何认证用户可查看
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CourseDto>> getAllCourses(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false) String sortParam) { // 默认按名称升序

        Pageable pageable;
        if (StringUtils.hasText(sortParam)) {
            String[] parts = sortParam.split(",", 2);
            String property = parts[0].trim();
            if (StringUtils.hasText(property)) {
                Sort.Direction direction = Sort.Direction.ASC;
                if (parts.length > 1 && StringUtils.hasText(parts[1].trim())) {
                    try {
                        direction = Sort.Direction.fromString(parts[1].trim().toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }
                pageable = PageRequest.of(page, size, Sort.by(direction, property));
            } else {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")); // 默认排序
            }
        } else {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")); // 默认排序
        }
        return ResponseEntity.ok(courseService.getAllCourses(pageable, query));
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseDto>> getAllCoursesList() {
        return ResponseEntity.ok(courseService.getAllCoursesList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')") // 管理员或教师可更新
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        CourseDto updatedCourse = courseService.updateCourse(id, courseDto);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 通常只有管理员能删除课程
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok("课程 ID: " + id + " 已成功删除。");
    }
}