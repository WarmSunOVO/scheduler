package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.payload.AiSuggestionResponseDto;
import com.example.intelligent_course_scheduler.payload.NaturalLanguageInputDto;

public interface AiHelperService {
    AiSuggestionResponseDto processNaturalLanguageInput(NaturalLanguageInputDto inputDto);
}