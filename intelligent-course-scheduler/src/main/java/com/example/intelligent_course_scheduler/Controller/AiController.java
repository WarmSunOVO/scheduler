package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.payload.AiSuggestionResponseDto;
import com.example.intelligent_course_scheduler.payload.NaturalLanguageInputDto;
import com.example.intelligent_course_scheduler.service.AiHelperService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiHelperService aiHelperService;

    @Autowired
    public AiController(AiHelperService aiHelperService) {
        this.aiHelperService = aiHelperService;
    }

    @PostMapping("/process-text")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')") // 允许教师也使用此功能
    public ResponseEntity<AiSuggestionResponseDto> processText(
            @Valid @RequestBody NaturalLanguageInputDto inputDto) {
        AiSuggestionResponseDto suggestion = aiHelperService.processNaturalLanguageInput(inputDto);
        return ResponseEntity.ok(suggestion);
    }
}