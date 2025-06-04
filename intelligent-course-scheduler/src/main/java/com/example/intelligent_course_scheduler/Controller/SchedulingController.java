package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.scheduling.ScheduleSolutionDto;
import com.example.intelligent_course_scheduler.scheduling.SchedulingRequestDto;
import com.example.intelligent_course_scheduler.scheduling.SchedulingException; // 引入自定义异常
import com.example.intelligent_course_scheduler.exception.ResourceNotFoundException; // 引入自定义异常
import com.example.intelligent_course_scheduler.service.AutoSchedulingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/scheduling")
public class SchedulingController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingController.class);
    private final AutoSchedulingService autoSchedulingService;
    // ClassroomRepository 不再需要在此 Controller 中注入，因为 Service 已处理 DTO 转换

    @Autowired
    public SchedulingController(AutoSchedulingService autoSchedulingService) {
        this.autoSchedulingService = autoSchedulingService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ScheduleSolutionDto> generateSchedule(
            @Valid @RequestBody SchedulingRequestDto schedulingRequestDto) {
        try {
            ScheduleSolutionDto solutionDto = autoSchedulingService.generateScheduleForSemester(schedulingRequestDto.getSemesterId());
            // Service 直接返回 DTO，Controller 直接使用
            return ResponseEntity.ok(solutionDto);
        } catch (SchedulingException e) { // 捕获自定义排课异常
            logger.error("Scheduling failed: {}", e.getMessage(), e);
            ScheduleSolutionDto errorSolution = new ScheduleSolutionDto();
            errorSolution.setSuccess(false);
            errorSolution.setMessage("排课失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorSolution); // 或者更具体的错误码
        } catch (ResourceNotFoundException e) { // 捕获资源未找到异常
            logger.error("Resource not found during scheduling: {}", e.getMessage(), e);
            ScheduleSolutionDto errorSolution = new ScheduleSolutionDto();
            errorSolution.setSuccess(false);
            errorSolution.setMessage("排课所需资源未找到: " + e.getMessage());
            return ResponseEntity.status(404).body(errorSolution);
        } catch (Exception e) { // 捕获其他未知异常
            logger.error("Unexpected error during scheduling: {}", e.getMessage(), e);
            ScheduleSolutionDto errorSolution = new ScheduleSolutionDto();
            errorSolution.setSuccess(false);
            errorSolution.setMessage("排课过程中发生未知内部错误，请联系管理员。");
            return ResponseEntity.status(500).body(errorSolution);
        }
    }
}