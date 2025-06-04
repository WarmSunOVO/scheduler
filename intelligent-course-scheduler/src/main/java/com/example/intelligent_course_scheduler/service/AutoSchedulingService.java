package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.scheduling.ScheduleSolutionDto;
import com.example.intelligent_course_scheduler.scheduling.SchedulingException;
//自动排课服务
public interface AutoSchedulingService {
    ScheduleSolutionDto generateScheduleForSemester(Long semesterId) throws SchedulingException;
}
