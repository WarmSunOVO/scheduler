package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.payload.TeacherUnavailabilityRequestDto;
import com.example.intelligent_course_scheduler.payload.TeacherUnavailabilityResponseDto;
import java.util.List;

public interface TeacherUnavailabilityService {
    TeacherUnavailabilityResponseDto createTeacherUnavailability(TeacherUnavailabilityRequestDto requestDto);
    List<TeacherUnavailabilityResponseDto> getTeacherUnavailabilitiesByTeacherAndSemester(Long teacherId, Long semesterId);
    List<TeacherUnavailabilityResponseDto> getTeacherUnavailabilitiesBySemester(Long semesterId);
    TeacherUnavailabilityResponseDto updateTeacherUnavailability(Long id, TeacherUnavailabilityRequestDto requestDto);
    void deleteTeacherUnavailability(Long id);
    // (可选) 获取指定教师所有学期的不可用时间
    List<TeacherUnavailabilityResponseDto> getTeacherUnavailabilitiesByTeacher(Long teacherId);
}