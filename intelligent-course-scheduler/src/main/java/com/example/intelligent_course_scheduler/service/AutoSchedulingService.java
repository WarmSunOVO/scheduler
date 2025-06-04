package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.scheduling.ScheduleSolutionDto;
import com.example.intelligent_course_scheduler.scheduling.SchedulingException;

public interface AutoSchedulingService {
    ScheduleSolutionDto generateScheduleForSemester(Long semesterId) throws SchedulingException; // <--- 确认是这个名字
}
