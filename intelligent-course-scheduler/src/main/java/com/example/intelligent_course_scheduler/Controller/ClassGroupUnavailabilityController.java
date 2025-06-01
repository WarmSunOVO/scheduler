package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.payload.ClassGroupUnavailabilityRequestDto;
import com.example.intelligent_course_scheduler.payload.ClassGroupUnavailabilityResponseDto;
import com.example.intelligent_course_scheduler.service.ClassGroupUnavailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class-group-unavailabilities")
public class ClassGroupUnavailabilityController {

    private final ClassGroupUnavailabilityService classGroupUnavailabilityService;

    @Autowired
    public ClassGroupUnavailabilityController(ClassGroupUnavailabilityService classGroupUnavailabilityService) {
        this.classGroupUnavailabilityService = classGroupUnavailabilityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ClassGroupUnavailabilityResponseDto> create(@Valid @RequestBody ClassGroupUnavailabilityRequestDto requestDto) {
        return new ResponseEntity<>(classGroupUnavailabilityService.createClassGroupUnavailability(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/target-group/{targetGroup}/semester/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')") // 教师可能需要查看班级不可用时间
    public ResponseEntity<List<ClassGroupUnavailabilityResponseDto>> getByTargetGroupAndSemester(
            @PathVariable String targetGroup, @PathVariable Long semesterId) {
        return ResponseEntity.ok(classGroupUnavailabilityService.getClassGroupUnavailabilitiesByTargetGroupAndSemester(targetGroup, semesterId));
    }

    @GetMapping("/semester/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ClassGroupUnavailabilityResponseDto>> getBySemester(@PathVariable Long semesterId) {
        return ResponseEntity.ok(classGroupUnavailabilityService.getClassGroupUnavailabilitiesBySemester(semesterId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ClassGroupUnavailabilityResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody ClassGroupUnavailabilityRequestDto requestDto) {
        return ResponseEntity.ok(classGroupUnavailabilityService.updateClassGroupUnavailability(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        classGroupUnavailabilityService.deleteClassGroupUnavailability(id);
        return ResponseEntity.ok("Class group unavailability with id " + id + " deleted successfully.");
    }
}