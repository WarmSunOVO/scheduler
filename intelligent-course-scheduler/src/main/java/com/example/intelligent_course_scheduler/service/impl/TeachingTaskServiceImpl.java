package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.Course;
import com.example.intelligent_course_scheduler.entity.Semester;
import com.example.intelligent_course_scheduler.entity.Teacher;
import com.example.intelligent_course_scheduler.entity.TeachingTask;
import com.example.intelligent_course_scheduler.payload.TeachingTaskCreateRequest;
import com.example.intelligent_course_scheduler.payload.TeachingTaskDto;
import com.example.intelligent_course_scheduler.payload.TeachingTaskUpdateRequest;
import com.example.intelligent_course_scheduler.repository.CourseRepository;
import com.example.intelligent_course_scheduler.repository.SemesterRepository;
import com.example.intelligent_course_scheduler.repository.TeacherRepository;
import com.example.intelligent_course_scheduler.repository.TeachingTaskRepository;
import com.example.intelligent_course_scheduler.service.TeachingTaskService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeachingTaskServiceImpl implements TeachingTaskService {

    @Autowired
    private TeachingTaskRepository teachingTaskRepository;
    @Autowired
    private SemesterRepository semesterRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private TeacherRepository teacherRepository;

    @Override
    @Transactional
    public TeachingTaskDto createTeachingTask(TeachingTaskCreateRequest request) {
        // 校验周学时、每周次数、每次节数的关系
        if (request.getHoursPerWeek() != (request.getSessionsPerWeek() * request.getSessionLength())) {
            throw new IllegalArgumentException("周学时、每周上课次数和每次连堂节数不匹配！");
        }

        if (teachingTaskRepository.existsBySemesterIdAndCourseIdAndTargetGroup(
                request.getSemesterId(), request.getCourseId(), request.getTargetGroup())) {
            throw new IllegalArgumentException("错误：该学期、该班级已存在此课程的教学任务！");
        }

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new RuntimeException("错误：找不到学期 ID: " + request.getSemesterId()));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("错误：找不到课程 ID: " + request.getCourseId()));
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("错误：找不到教师 ID: " + request.getTeacherId()));

        TeachingTask task = TeachingTask.builder()
                .semester(semester)
                .course(course)
                .teacher(teacher)
                .targetGroup(request.getTargetGroup())
                .hoursPerWeek(request.getHoursPerWeek())
                .sessionsPerWeek(request.getSessionsPerWeek())
                .sessionLength(request.getSessionLength())
                .requiredRoomType(StringUtils.hasText(request.getRequiredRoomType()) ? request.getRequiredRoomType() : course.getRequiredRoomType()) // 优先使用请求中的，否则用课程默认的
                .remarks(request.getRemarks())
                .build();

        return TeachingTaskDto.fromEntity(teachingTaskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeachingTaskDto> getTeachingTaskById(Long id) {
        return teachingTaskRepository.findById(id).map(TeachingTaskDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeachingTaskDto> getAllTeachingTasks(Pageable pageable, Long semesterId, String targetGroupQuery, Long courseId, Long teacherId) {
        Specification<TeachingTask> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (semesterId != null) {
                predicates.add(criteriaBuilder.equal(root.get("semester").get("id"), semesterId));
            }
            if (StringUtils.hasText(targetGroupQuery)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("targetGroup")), "%" + targetGroupQuery.toLowerCase() + "%"));
            }
            if (courseId != null) {
                predicates.add(criteriaBuilder.equal(root.get("course").get("id"), courseId));
            }
            if (teacherId != null) {
                predicates.add(criteriaBuilder.equal(root.get("teacher").get("id"), teacherId));
            }
            // 可以添加更多筛选条件，例如按课程名称、教师名称等（需要JOIN）
            // if (StringUtils.hasText(courseNameQuery)) {
            //    Join<TeachingTask, Course> courseJoin = root.join("course");
            //    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(courseJoin.get("name")), "%" + courseNameQuery.toLowerCase() + "%"));
            // }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return teachingTaskRepository.findAll(spec, pageable).map(TeachingTaskDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingTaskDto> getTasksBySemesterAndTargetGroup(Long semesterId, String targetGroup) {
        return teachingTaskRepository.findBySemesterIdAndTargetGroup(semesterId, targetGroup)
                .stream()
                .map(TeachingTaskDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public TeachingTaskDto updateTeachingTask(Long id, TeachingTaskUpdateRequest request) {
        TeachingTask existingTask = teachingTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错误：找不到教学任务 ID: " + id));

        // 校验周学时、每周次数、每次节数的关系
        if (request.getHoursPerWeek() != (request.getSessionsPerWeek() * request.getSessionLength())) {
            throw new IllegalArgumentException("周学时、每周上课次数和每次连堂节数不匹配！");
        }

        // 通常学期、课程、目标群体不允许修改，如果需要修改，应删除旧任务新建。这里只允许修改教师等。
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("错误：找不到教师 ID: " + request.getTeacherId()));

        existingTask.setTeacher(teacher);
        existingTask.setHoursPerWeek(request.getHoursPerWeek());
        existingTask.setSessionsPerWeek(request.getSessionsPerWeek());
        existingTask.setSessionLength(request.getSessionLength());

        String roomTypeToSet = request.getRequiredRoomType();
        if (!StringUtils.hasText(roomTypeToSet) && existingTask.getCourse() != null) { // 如果更新时没传，则使用课程的默认值
            roomTypeToSet = existingTask.getCourse().getRequiredRoomType();
        }
        existingTask.setRequiredRoomType(roomTypeToSet);
        existingTask.setRemarks(request.getRemarks());

        return TeachingTaskDto.fromEntity(teachingTaskRepository.save(existingTask));
    }

    @Override
    @Transactional
    public void deleteTeachingTask(Long id) {
        if (!teachingTaskRepository.existsById(id)) {
            throw new RuntimeException("错误：找不到教学任务 ID: " + id + "，无法删除");
        }
        // TODO: 实际项目中，删除教学任务前应检查是否已排课等
        teachingTaskRepository.deleteById(id);
    }
}