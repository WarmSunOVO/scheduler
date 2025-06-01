package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.ClassGroupUnavailability;
import com.example.intelligent_course_scheduler.entity.Semester;
import com.example.intelligent_course_scheduler.exception.ResourceNotFoundException;
import com.example.intelligent_course_scheduler.payload.ClassGroupUnavailabilityRequestDto;
import com.example.intelligent_course_scheduler.payload.ClassGroupUnavailabilityResponseDto;
import com.example.intelligent_course_scheduler.repository.ClassGroupUnavailabilityRepository;
import com.example.intelligent_course_scheduler.repository.SemesterRepository;
// 如果需要校验 targetGroup 是否存在于某个表中（例如 teaching_task 或一个独立的班级表），则需要对应 Repository
// import com.example.intelligent_course_scheduler.repository.TeachingTaskRepository;
import com.example.intelligent_course_scheduler.service.ClassGroupUnavailabilityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // <<-- 关键注解！！
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service // <<-- 确保这个注解存在！！
@Transactional
public class ClassGroupUnavailabilityServiceImpl implements ClassGroupUnavailabilityService {

    private final ClassGroupUnavailabilityRepository classGroupUnavailabilityRepository;
    private final SemesterRepository semesterRepository;
    // private final TeachingTaskRepository teachingTaskRepository; // 如果需要校验 targetGroup

    @Autowired
    public ClassGroupUnavailabilityServiceImpl(
            ClassGroupUnavailabilityRepository classGroupUnavailabilityRepository,
            SemesterRepository semesterRepository
            // TeachingTaskRepository teachingTaskRepository // 注入
    ) {
        this.classGroupUnavailabilityRepository = classGroupUnavailabilityRepository;
        this.semesterRepository = semesterRepository;
        // this.teachingTaskRepository = teachingTaskRepository;
    }

    @Override
    public ClassGroupUnavailabilityResponseDto createClassGroupUnavailability(ClassGroupUnavailabilityRequestDto requestDto) {
        Semester semester = semesterRepository.findById(requestDto.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", requestDto.getSemesterId()));

        // TODO: (可选) 校验 targetGroup 是否是一个有效的教学对象标识
        // if (!teachingTaskRepository.existsByTargetGroupAndSemesterId(requestDto.getTargetGroup(), requestDto.getSemesterId())) {
        //     throw new ResourceNotFoundException("TeachingTask targetGroup", "identifier", requestDto.getTargetGroup() + " for semester " + requestDto.getSemesterId());
        // }

        validatePeriods(requestDto.getStartPeriod(), requestDto.getEndPeriod(), semester);

        ClassGroupUnavailability unavailability = new ClassGroupUnavailability();
        mapDtoToEntity(requestDto, unavailability, semester);


        ClassGroupUnavailability savedUnavailability = classGroupUnavailabilityRepository.save(unavailability);
        return mapEntityToDto(savedUnavailability);
    }

    @Override
    public List<ClassGroupUnavailabilityResponseDto> getClassGroupUnavailabilitiesByTargetGroupAndSemester(String targetGroup, Long semesterId) {
        if (!semesterRepository.existsById(semesterId)) { // 校验学期是否存在
            throw new ResourceNotFoundException("Semester", "id", semesterId);
        }
        // TODO: (可选) 校验 targetGroup 是否是一个有效的教学对象标识

        List<ClassGroupUnavailability> unavailabilities = classGroupUnavailabilityRepository.findByTargetGroupAndSemesterIdAndIsActiveTrue(targetGroup, semesterId);
        return unavailabilities.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<ClassGroupUnavailabilityResponseDto> getClassGroupUnavailabilitiesBySemester(Long semesterId) {
        if (!semesterRepository.existsById(semesterId)) {
            throw new ResourceNotFoundException("Semester", "id", semesterId);
        }
        List<ClassGroupUnavailability> unavailabilities = classGroupUnavailabilityRepository.findBySemesterIdAndIsActiveTrue(semesterId);
        return unavailabilities.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    public ClassGroupUnavailabilityResponseDto updateClassGroupUnavailability(Long id, ClassGroupUnavailabilityRequestDto requestDto) {
        ClassGroupUnavailability unavailability = classGroupUnavailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGroupUnavailability", "id", id));

        Semester semester = semesterRepository.findById(requestDto.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", requestDto.getSemesterId()));

        // TODO: (可选) 校验 targetGroup
        validatePeriods(requestDto.getStartPeriod(), requestDto.getEndPeriod(), semester);

        mapDtoToEntity(requestDto, unavailability, semester);

        ClassGroupUnavailability updatedUnavailability = classGroupUnavailabilityRepository.save(unavailability);
        return mapEntityToDto(updatedUnavailability);
    }

    @Override
    public void deleteClassGroupUnavailability(Long id) {
        ClassGroupUnavailability unavailability = classGroupUnavailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGroupUnavailability", "id", id));
        classGroupUnavailabilityRepository.delete(unavailability);
    }

    private void mapDtoToEntity(ClassGroupUnavailabilityRequestDto dto, ClassGroupUnavailability entity, Semester semester) {
        entity.setTargetGroup(dto.getTargetGroup());
        entity.setSemester(semester);
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setStartPeriod(dto.getStartPeriod());
        entity.setEndPeriod(dto.getEndPeriod());
        entity.setReason(dto.getReason());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        } else if (entity.getId() == null) {
            entity.setIsActive(true);
        }
    }

    private ClassGroupUnavailabilityResponseDto mapEntityToDto(ClassGroupUnavailability unavailability) {
        ClassGroupUnavailabilityResponseDto dto = new ClassGroupUnavailabilityResponseDto();
        BeanUtils.copyProperties(unavailability, dto, "semester");
        // 对于 targetGroup，它本身就是标识，不需要像 teacherName 或 roomName 那样去关联查询显示名称
        dto.setSemesterId(unavailability.getSemester().getId());
        dto.setSemesterDisplay(unavailability.getSemester().getAcademicYear() + " " + unavailability.getSemester().getName());
        return dto;
    }

    private void validatePeriods(Integer startPeriod, Integer endPeriod, Semester semester) {
        if (startPeriod > endPeriod) {
            throw new IllegalArgumentException("开始节次不能大于结束节次。");
        }
        if (semester.getPeriodsPerDay() != null) {
            if (startPeriod < 1 || startPeriod > semester.getPeriodsPerDay()) {
                throw new IllegalArgumentException("开始节次超出了学期定义的每日有效节次范围 (1-" + semester.getPeriodsPerDay() + ")。");
            }
            if (endPeriod < 1 || endPeriod > semester.getPeriodsPerDay()) {
                throw new IllegalArgumentException("结束节次超出了学期定义的每日有效节次范围 (1-" + semester.getPeriodsPerDay() + ")。");
            }
        } else {
            if (startPeriod < 1 || endPeriod < 1) {
                throw new IllegalArgumentException("节次必须是正数。");
            }
        }
    }
}