package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.entity.Classroom;
import com.example.intelligent_course_scheduler.service.ClassroomService;
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
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    @Autowired
    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @PostMapping
    public ResponseEntity<?> createClassroom(@RequestBody Classroom classroom) {
        if (classroom.getRoomNumber() == null || classroom.getRoomNumber().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("教室编号不能为空");
        }
        if (classroom.getCapacity() == null || classroom.getCapacity() <= 0) {
            return ResponseEntity.badRequest().body("教室容量必须大于0");
        }
        Classroom createdClassroom = classroomService.createClassroom(classroom);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClassroom);
    }

    @GetMapping("/api/classrooms")
    public ResponseEntity<List<Classroom>> getAllClassroomsList() { // Renamed to avoid clash if you had another /all
        List<Classroom> classrooms = classroomService.getAllClassrooms();
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping
    public ResponseEntity<Page<Classroom>> getClassrooms(
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort.length > 1 ? sort[1] : "desc");
        Sort.Order order = new Sort.Order(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<Classroom> classroomPage = classroomService.getClassrooms(roomNumber, type, status, pageable);
        return ResponseEntity.ok(classroomPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Classroom> getClassroomById(@PathVariable Long id) {
        Optional<Classroom> classroom = classroomService.getClassroomById(id);
        return classroom.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClassroom(@PathVariable Long id, @RequestBody Classroom classroomDetails) {
        if (classroomDetails.getCapacity() != null && classroomDetails.getCapacity() <= 0) {
            return ResponseEntity.badRequest().body("教室容量必须大于0");
        }
        Classroom updatedClassroom = classroomService.updateClassroom(id, classroomDetails);
        return ResponseEntity.ok(updatedClassroom);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        Map<String, Object> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        response.put("message", "教室 ID: " + id + " 已成功删除。");
        return ResponseEntity.ok(response);
    }
}