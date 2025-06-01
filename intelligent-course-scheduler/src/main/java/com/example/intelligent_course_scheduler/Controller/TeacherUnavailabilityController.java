package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.payload.TeacherUnavailabilityRequestDto;
import com.example.intelligent_course_scheduler.payload.TeacherUnavailabilityResponseDto;
import com.example.intelligent_course_scheduler.service.TeacherUnavailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 如果需要权限控制
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher-unavailabilities")
public class TeacherUnavailabilityController {

    private final TeacherUnavailabilityService teacherUnavailabilityService;

    @Autowired
    public TeacherUnavailabilityController(TeacherUnavailabilityService teacherUnavailabilityService) {
        this.teacherUnavailabilityService = teacherUnavailabilityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // 示例权限控制
    public ResponseEntity<TeacherUnavailabilityResponseDto> createTeacherUnavailability(@Valid @RequestBody TeacherUnavailabilityRequestDto requestDto) {
        TeacherUnavailabilityResponseDto createdUnavailability = teacherUnavailabilityService.createTeacherUnavailability(requestDto);
        return new ResponseEntity<>(createdUnavailability, HttpStatus.CREATED);
    }

    // 查询特定教师在特定学期的不可用时间
    @GetMapping("/teacher/{teacherId}/semester/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    public ResponseEntity<List<TeacherUnavailabilityResponseDto>> getByTeacherAndSemester(
            @PathVariable Long teacherId,
            @PathVariable Long semesterId) {
        // TODO: 如果是教师角色，应该校验 teacherId 是否为当前登录用户 (如果适用)
        List<TeacherUnavailabilityResponseDto> unavailabilities = teacherUnavailabilityService.getTeacherUnavailabilitiesByTeacherAndSemester(teacherId, semesterId);
        return ResponseEntity.ok(unavailabilities);
    }

    // 查询特定学期所有教师的不可用时间 (可能数据量大，考虑分页或更具体的筛选)
    @GetMapping("/semester/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TeacherUnavailabilityResponseDto>> getBySemester(@PathVariable Long semesterId) {
        List<TeacherUnavailabilityResponseDto> unavailabilities = teacherUnavailabilityService.getTeacherUnavailabilitiesBySemester(semesterId);
        return ResponseEntity.ok(unavailabilities);
    }

    // 查询特定教师所有的不可用时间
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER','TEACHER')")
    public ResponseEntity<List<TeacherUnavailabilityResponseDto>> getByTeacher(@PathVariable Long teacherId){
        // TODO: 如果是教师角色，应该校验 teacherId 是否为当前登录用户 (如果适用)
        List<TeacherUnavailabilityResponseDto> unavailabilities = teacherUnavailabilityService.getTeacherUnavailabilitiesByTeacher(teacherId);
        return ResponseEntity.ok(unavailabilities);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TeacherUnavailabilityResponseDto> updateTeacherUnavailability(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUnavailabilityRequestDto requestDto) {
        TeacherUnavailabilityResponseDto updatedUnavailability = teacherUnavailabilityService.updateTeacherUnavailability(id, requestDto);
        return ResponseEntity.ok(updatedUnavailability);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> deleteTeacherUnavailability(@PathVariable Long id) {
        teacherUnavailabilityService.deleteTeacherUnavailability(id);
        return ResponseEntity.ok("Teacher unavailability with id " + id + " deleted successfully.");
    }
}