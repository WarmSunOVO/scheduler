package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.entity.Teacher;
import com.example.intelligent_course_scheduler.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @Autowired
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // 1. 创建新教师
    @PostMapping
    public ResponseEntity<?> createTeacher(@RequestBody Teacher teacher) {
        // 基础校验，更复杂的应在Service层或使用Bean Validation
        if (!StringUtils.hasText(teacher.getTeacherIdNumber())) {
            return ResponseEntity.badRequest().body("教师工号不能为空");
        }
        if (!StringUtils.hasText(teacher.getName())) {
            return ResponseEntity.badRequest().body("教师姓名不能为空");
        }
        // Service层已有更详细的校验和异常处理
        Teacher createdTeacher = teacherService.createTeacher(teacher);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeacher);
    }

    // 2. 获取所有教师列表 (通常分页更好)
    @GetMapping("/all")
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    // 3. 分页获取教师列表
    @GetMapping
    public ResponseEntity<Page<Teacher>> getTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort.length > 1 ? sort[1] : "desc");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<Teacher> teacherPage = teacherService.getTeachers(pageable);
        return ResponseEntity.ok(teacherPage);
    }

    // 4. 根据ID获取教师信息
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        Optional<Teacher> teacher = teacherService.getTeacherById(id);
        return teacher.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 5. 更新教师信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody Teacher teacherDetails) {
        if (teacherDetails.getName() != null && !StringUtils.hasText(teacherDetails.getName())) {
            return ResponseEntity.badRequest().body("教师姓名不能为空");
        }
        // Service层已有更详细的校验和异常处理
        Teacher updatedTeacher = teacherService.updateTeacher(id, teacherDetails);
        return ResponseEntity.ok(updatedTeacher);
    }

    // 6. 删除教师
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        // Service层已有校验和异常处理
        teacherService.deleteTeacher(id);
        Map<String, Object> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        response.put("message", "教师 ID: " + id + " 已成功删除。");
        return ResponseEntity.ok(response);
    }
}