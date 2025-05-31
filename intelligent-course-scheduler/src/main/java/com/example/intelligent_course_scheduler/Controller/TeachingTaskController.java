package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.payload.TeachingTaskCreateRequest;
import com.example.intelligent_course_scheduler.payload.TeachingTaskDto;
import com.example.intelligent_course_scheduler.payload.TeachingTaskUpdateRequest;
import com.example.intelligent_course_scheduler.service.TeachingTaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/teaching-tasks")
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')") // 假设管理员和教师可以管理教学任务
public class TeachingTaskController {

    private static final Logger logger = LoggerFactory.getLogger(TeachingTaskController.class);

    @Autowired
    private TeachingTaskService teachingTaskService;

    @PostMapping
    public ResponseEntity<TeachingTaskDto> createTeachingTask(@Valid @RequestBody TeachingTaskCreateRequest request) {
        logger.info("Request to create teaching task for target group: {}", request.getTargetGroup());
        TeachingTaskDto createdTask = teachingTaskService.createTeachingTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeachingTaskDto> getTeachingTaskById(@PathVariable Long id) {
        logger.info("Request to get teaching task by ID: {}", id);
        return teachingTaskService.getTeachingTaskById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<TeachingTaskDto>> getAllTeachingTasks(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String targetGroup, // 模糊查询
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false) String sortParam) {

        logger.info("Request to get all teaching tasks. Page: {}, Size: {}, Sort: {}, SemesterId: {}, TargetGroup: {}, CourseId: {}, TeacherId: {}",
                page, size, sortParam, semesterId, targetGroup, courseId, teacherId);

        Pageable pageable;
        List<Sort.Order> orders = new ArrayList<>();
        if (StringUtils.hasText(sortParam)) {
            String[] parts = sortParam.split(",", 2);
            String property = parts[0].trim();
            if (StringUtils.hasText(property)) {
                Sort.Direction direction = Sort.Direction.ASC;
                if (parts.length > 1 && StringUtils.hasText(parts[1].trim())) {
                    try {
                        direction = Sort.Direction.fromString(parts[1].trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid sort direction: '{}' for property '{}'. Defaulting to ASC.", parts[1], property);
                    }
                }
                orders.add(new Sort.Order(direction, property));
            }
        }

        if (!orders.isEmpty()) {
            pageable = PageRequest.of(page, size, Sort.by(orders));
        } else {
            // 默认可以按创建时间降序或ID降序
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        }

        Page<TeachingTaskDto> taskPage = teachingTaskService.getAllTeachingTasks(pageable, semesterId, targetGroup, courseId, teacherId);
        return ResponseEntity.ok(taskPage);
    }

    @GetMapping("/by-semester-group") // 按学期和班级获取不分页列表
    public ResponseEntity<List<TeachingTaskDto>> getTasksBySemesterAndTargetGroup(
            @RequestParam Long semesterId,
            @RequestParam String targetGroup) {
        logger.info("Request to get tasks for semesterId: {} and targetGroup: {}", semesterId, targetGroup);
        return ResponseEntity.ok(teachingTaskService.getTasksBySemesterAndTargetGroup(semesterId, targetGroup));
    }


    @PutMapping("/{id}")
    public ResponseEntity<TeachingTaskDto> updateTeachingTask(@PathVariable Long id, @Valid @RequestBody TeachingTaskUpdateRequest request) {
        logger.info("Request to update teaching task with ID: {}", id);
        TeachingTaskDto updatedTask = teachingTaskService.updateTeachingTask(id, request);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeachingTask(@PathVariable Long id) {
        logger.info("Request to delete teaching task with ID: {}", id);
        teachingTaskService.deleteTeachingTask(id);
        return ResponseEntity.ok("教学任务 ID: " + id + " 已成功删除。");
    }
}