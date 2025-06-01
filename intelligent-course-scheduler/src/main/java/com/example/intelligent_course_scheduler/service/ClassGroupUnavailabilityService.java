package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.payload.ClassGroupUnavailabilityRequestDto;
import com.example.intelligent_course_scheduler.payload.ClassGroupUnavailabilityResponseDto;
import java.util.List;

public interface ClassGroupUnavailabilityService {
    ClassGroupUnavailabilityResponseDto createClassGroupUnavailability(ClassGroupUnavailabilityRequestDto requestDto);
    List<ClassGroupUnavailabilityResponseDto> getClassGroupUnavailabilitiesByTargetGroupAndSemester(String targetGroup, Long semesterId);
    List<ClassGroupUnavailabilityResponseDto> getClassGroupUnavailabilitiesBySemester(Long semesterId);
    ClassGroupUnavailabilityResponseDto updateClassGroupUnavailability(Long id, ClassGroupUnavailabilityRequestDto requestDto);
    void deleteClassGroupUnavailability(Long id);
}