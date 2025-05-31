package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.payload.TeachingTaskCreateRequest;
import com.example.intelligent_course_scheduler.payload.TeachingTaskDto;
import com.example.intelligent_course_scheduler.payload.TeachingTaskUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TeachingTaskService {
    TeachingTaskDto createTeachingTask(TeachingTaskCreateRequest request);
    Optional<TeachingTaskDto> getTeachingTaskById(Long id);
    // 根据学期、班级、教师、课程等条件查询任务列表
    Page<TeachingTaskDto> getAllTeachingTasks(Pageable pageable, Long semesterId, String targetGroupQuery, Long courseId, Long teacherId);
    List<TeachingTaskDto> getTasksBySemesterAndTargetGroup(Long semesterId, String targetGroup); // 不分页
    TeachingTaskDto updateTeachingTask(Long id, TeachingTaskUpdateRequest request);
    void deleteTeachingTask(Long id);
}