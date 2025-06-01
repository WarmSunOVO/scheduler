package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.Classroom; // 假设实体名为 Classroom
import com.example.intelligent_course_scheduler.entity.RoomUnavailability;
import com.example.intelligent_course_scheduler.entity.Semester;
import com.example.intelligent_course_scheduler.exception.ResourceNotFoundException;
import com.example.intelligent_course_scheduler.payload.RoomUnavailabilityRequestDto; // 需要创建这个 DTO
import com.example.intelligent_course_scheduler.payload.RoomUnavailabilityResponseDto; // 需要创建这个 DTO
import com.example.intelligent_course_scheduler.repository.ClassroomRepository; // 假设您有 ClassroomRepository
import com.example.intelligent_course_scheduler.repository.RoomUnavailabilityRepository;
import com.example.intelligent_course_scheduler.repository.SemesterRepository;
import com.example.intelligent_course_scheduler.service.RoomUnavailabilityService; // 需要创建这个 Service 接口
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomUnavailabilityServiceImpl implements RoomUnavailabilityService {

    private final RoomUnavailabilityRepository roomUnavailabilityRepository;
    private final ClassroomRepository classroomRepository;
    private final SemesterRepository semesterRepository;

    @Autowired
    public RoomUnavailabilityServiceImpl(RoomUnavailabilityRepository roomUnavailabilityRepository,
                                         ClassroomRepository classroomRepository,
                                         SemesterRepository semesterRepository) {
        this.roomUnavailabilityRepository = roomUnavailabilityRepository;
        this.classroomRepository = classroomRepository;
        this.semesterRepository = semesterRepository;
    }

    @Override
    public RoomUnavailabilityResponseDto createRoomUnavailability(RoomUnavailabilityRequestDto requestDto) {
        Classroom classroom = classroomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", requestDto.getRoomId()));
        Semester semester = semesterRepository.findById(requestDto.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", requestDto.getSemesterId()));

        validatePeriods(requestDto.getStartPeriod(), requestDto.getEndPeriod(), semester);

        RoomUnavailability unavailability = new RoomUnavailability();
        mapDtoToEntity(requestDto, unavailability, classroom, semester);

        RoomUnavailability savedUnavailability = roomUnavailabilityRepository.save(unavailability);
        return mapEntityToDto(savedUnavailability);
    }

    @Override
    public List<RoomUnavailabilityResponseDto> getRoomUnavailabilitiesByRoom(Long roomId) {
        if (!classroomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Classroom", "id", roomId);
        }
        // VVVVVV 修改这里的方法调用 VVVVVV
        List<RoomUnavailability> unavailabilities = roomUnavailabilityRepository.findByClassroomIdAndIsActiveTrue(roomId);
        // ^^^^^^ 修改这里的方法调用 ^^^^^^
        return unavailabilities.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<RoomUnavailabilityResponseDto> getRoomUnavailabilitiesBySemester(Long semesterId) {
        if (!semesterRepository.existsById(semesterId)) {
            throw new ResourceNotFoundException("Semester", "id", semesterId);
        }
        List<RoomUnavailability> unavailabilities = roomUnavailabilityRepository.findBySemesterIdAndIsActiveTrue(semesterId);
        return unavailabilities.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }


    @Override
    public RoomUnavailabilityResponseDto updateRoomUnavailability(Long id, RoomUnavailabilityRequestDto requestDto) {
        RoomUnavailability unavailability = roomUnavailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomUnavailability", "id", id));

        Classroom classroom = classroomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", requestDto.getRoomId()));
        Semester semester = semesterRepository.findById(requestDto.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", requestDto.getSemesterId()));

        validatePeriods(requestDto.getStartPeriod(), requestDto.getEndPeriod(), semester);

        mapDtoToEntity(requestDto, unavailability, classroom, semester);

        RoomUnavailability updatedUnavailability = roomUnavailabilityRepository.save(unavailability);
        return mapEntityToDto(updatedUnavailability);
    }

    @Override
    public void deleteRoomUnavailability(Long id) {
        RoomUnavailability unavailability = roomUnavailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomUnavailability", "id", id));
        roomUnavailabilityRepository.delete(unavailability);
    }

    @Override
    public List<RoomUnavailabilityResponseDto> getRoomUnavailabilitiesByRoomAndSemester(Long roomId, Long semesterId) {
        if (!classroomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Classroom", "id", roomId);
        }
        if (!semesterRepository.existsById(semesterId)) {
            throw new ResourceNotFoundException("Semester", "id", semesterId);
        }
        List<RoomUnavailability> unavailabilities = roomUnavailabilityRepository.findByClassroomIdAndSemesterIdAndIsActiveTrue(roomId, semesterId);
        return unavailabilities.stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }


    // Helper method to map DTO to Entity
    private void mapDtoToEntity(RoomUnavailabilityRequestDto dto, RoomUnavailability entity, Classroom classroom, Semester semester) {
        entity.setClassroom(classroom);
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



    // 在 RoomUnavailabilityServiceImpl.java 的 mapEntityToDto 方法中
    private RoomUnavailabilityResponseDto mapEntityToDto(RoomUnavailability unavailability) {
        RoomUnavailabilityResponseDto dto = new RoomUnavailabilityResponseDto();
        BeanUtils.copyProperties(unavailability, dto, "classroom", "semester");
        dto.setRoomId(unavailability.getClassroom().getId());

        // VVVVVV 修改这一部分 VVVVVV
        // dto.setRoomName(unavailability.getClassroom().getName()); // 原来的代码，会报错
        // 使用 roomNumber 字段的值来填充 roomName，或者如果 DTO 中有 roomNumber 字段，直接设置
        if (unavailability.getClassroom() != null) { // 做一个空检查更安全
            dto.setRoomName(unavailability.getClassroom().getRoomNumber()); // 使用 getRoomNumber()
            // 或者，如果您的 RoomUnavailabilityResponseDto 中有 roomNumber 字段:
            // dto.setRoomNumber(unavailability.getClassroom().getRoomNumber());
        }
        // ^^^^^^ 修改这一部分 ^^^^^^

        dto.setSemesterId(unavailability.getSemester().getId());
        dto.setSemesterDisplay(unavailability.getSemester().getAcademicYear() + " " + unavailability.getSemester().getName());
        return dto;
    }

    // Re-use period validation logic (could be moved to a common utility or base service if many services need it)
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