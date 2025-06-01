package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.Semester;
import com.example.intelligent_course_scheduler.entity.Teacher;
import com.example.intelligent_course_scheduler.entity.TeacherUnavailability;
import com.example.intelligent_course_scheduler.exception.ResourceNotFoundException; // 确保这个异常类已创建
import com.example.intelligent_course_scheduler.payload.TeacherUnavailabilityRequestDto;
import com.example.intelligent_course_scheduler.payload.TeacherUnavailabilityResponseDto;
import com.example.intelligent_course_scheduler.repository.SemesterRepository;
import com.example.intelligent_course_scheduler.repository.TeacherRepository; // 确保您有 TeacherRepository
import com.example.intelligent_course_scheduler.repository.TeacherUnavailabilityRepository;
import com.example.intelligent_course_scheduler.service.TeacherUnavailabilityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherUnavailabilityServiceImpl implements TeacherUnavailabilityService {

    private final TeacherUnavailabilityRepository teacherUnavailabilityRepository;
    private final TeacherRepository teacherRepository;
    private final SemesterRepository semesterRepository;

    @Autowired
    public TeacherUnavailabilityServiceImpl(TeacherUnavailabilityRepository teacherUnavailabilityRepository,
                                            TeacherRepository teacherRepository,
                                            SemesterRepository semesterRepository) {
        this.teacherUnavailabilityRepository = teacherUnavailabilityRepository;
        this.teacherRepository = teacherRepository;
        this.semesterRepository = semesterRepository;
    }

    @Override
    public TeacherUnavailabilityResponseDto createTeacherUnavailability(TeacherUnavailabilityRequestDto requestDto) {
        Teacher teacher = teacherRepository.findById(requestDto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", requestDto.getTeacherId()));
        Semester semester = semesterRepository.findById(requestDto.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", requestDto.getSemesterId()));

        validatePeriods(requestDto.getStartPeriod(), requestDto.getEndPeriod(), semester);
        // TODO: (可选) 检查该教师在此时段是否已有不可用记录或已安排课程（更复杂的校验）

        TeacherUnavailability unavailability = new TeacherUnavailability();
        mapDtoToEntity(requestDto, unavailability, teacher, semester);

        TeacherUnavailability savedUnavailability = teacherUnavailabilityRepository.save(unavailability);
        return mapEntityToDto(savedUnavailability);
    }

    @Override
    public List<TeacherUnavailabilityResponseDto> getTeacherUnavailabilitiesByTeacherAndSemester(Long teacherId, Long semesterId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Teacher", "id", teacherId);
        }
        if (!semesterRepository.existsById(semesterId)) {
            throw new ResourceNotFoundException("Semester", "id", semesterId);
        }
        List<TeacherUnavailability> unavailabilities = teacherUnavailabilityRepository.findByTeacherIdAndSemesterIdAndIsActiveTrue(teacherId, semesterId);
        return unavailabilities.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<TeacherUnavailabilityResponseDto> getTeacherUnavailabilitiesBySemester(Long semesterId) {
        if (!semesterRepository.existsById(semesterId)) {
            throw new ResourceNotFoundException("Semester", "id", semesterId);
        }
        List<TeacherUnavailability> unavailabilities = teacherUnavailabilityRepository.findBySemesterIdAndIsActiveTrue(semesterId);
        return unavailabilities.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    public TeacherUnavailabilityResponseDto updateTeacherUnavailability(Long id, TeacherUnavailabilityRequestDto requestDto) {
        TeacherUnavailability unavailability = teacherUnavailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherUnavailability", "id", id));

        Teacher teacher = teacherRepository.findById(requestDto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", requestDto.getTeacherId()));
        Semester semester = semesterRepository.findById(requestDto.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", requestDto.getSemesterId()));

        validatePeriods(requestDto.getStartPeriod(), requestDto.getEndPeriod(), semester);
        // TODO: (可选) 更新时检查时间段冲突（排除自身）

        mapDtoToEntity(requestDto, unavailability, teacher, semester);

        TeacherUnavailability updatedUnavailability = teacherUnavailabilityRepository.save(unavailability);
        return mapEntityToDto(updatedUnavailability);
    }

    @Override
    public void deleteTeacherUnavailability(Long id) {
        TeacherUnavailability unavailability = teacherUnavailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherUnavailability", "id", id));
        teacherUnavailabilityRepository.delete(unavailability);
    }

    @Override
    public List<TeacherUnavailabilityResponseDto> getTeacherUnavailabilitiesByTeacher(Long teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Teacher", "id", teacherId);
        }
        List<TeacherUnavailability> unavailabilities = teacherUnavailabilityRepository.findByTeacherIdAndIsActiveTrue(teacherId);
        return unavailabilities.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    // Helper method to map DTO to Entity for Create/Update
    private void mapDtoToEntity(TeacherUnavailabilityRequestDto dto, TeacherUnavailability entity, Teacher teacher, Semester semester) {
        entity.setTeacher(teacher);
        entity.setSemester(semester);
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setStartPeriod(dto.getStartPeriod());
        entity.setEndPeriod(dto.getEndPeriod());
        entity.setReason(dto.getReason());
        if (dto.getIsActive() != null) { // 允许 dto 中不传递 isActive，则保持 entity 原有值或默认值
            entity.setIsActive(dto.getIsActive());
        } else if (entity.getId() == null) { // 如果是新建且 dto 中没有 isActive，则默认为 true
            entity.setIsActive(true);
        }
    }

    // Helper method to map Entity to DTO
    private TeacherUnavailabilityResponseDto mapEntityToDto(TeacherUnavailability unavailability) {
        TeacherUnavailabilityResponseDto dto = new TeacherUnavailabilityResponseDto();
        BeanUtils.copyProperties(unavailability, dto, "teacher", "semester"); // 复制基本属性，排除关联对象
        dto.setTeacherId(unavailability.getTeacher().getId());
        dto.setTeacherName(unavailability.getTeacher().getName()); // 假设 Teacher 实体有 getName()
        dto.setSemesterId(unavailability.getSemester().getId());
        dto.setSemesterDisplay(unavailability.getSemester().getAcademicYear() + " " + unavailability.getSemester().getName());
        return dto;
    }

    // Helper method for period validation
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
            // 如果学期没有定义 periodsPerDay，至少确保节次是正数
            if (startPeriod < 1 || endPeriod < 1) {
                throw new IllegalArgumentException("节次必须是正数。");
            }
        }
    }
}