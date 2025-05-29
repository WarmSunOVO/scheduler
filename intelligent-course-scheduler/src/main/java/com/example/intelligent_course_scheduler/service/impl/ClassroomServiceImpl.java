package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.Classroom;
import com.example.intelligent_course_scheduler.repository.ClassroomRepository;
import com.example.intelligent_course_scheduler.service.ClassroomService;
import jakarta.persistence.criteria.Predicate; // JPA Criteria API Predicate
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Spring Data JPA Specification
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Spring Framework StringUtils

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;

    @Autowired
    public ClassroomServiceImpl(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @Override
    @Transactional
    public Classroom createClassroom(Classroom classroom) {
        if (classroomRepository.findByRoomNumber(classroom.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("教室编号 '" + classroom.getRoomNumber() + "' 已存在！");
        }
        if (classroom.getCapacity() == null || classroom.getCapacity() <= 0) {
            throw new IllegalArgumentException("教室容量必须大于0！");
        }
        return classroomRepository.save(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Classroom> getClassroomById(Long id) {
        return classroomRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Classroom> getClassroomByRoomNumber(String roomNumber) {
        return classroomRepository.findByRoomNumber(roomNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Classroom> getAllClassrooms() {
        return classroomRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Classroom> getClassrooms(String roomNumber, String type, String status, Pageable pageable) {
        Specification<Classroom> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(roomNumber)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("roomNumber")), "%" + roomNumber.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(type)) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return classroomRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public Classroom updateClassroom(Long id, Classroom classroomDetails) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到ID为 " + id + " 的教室"));

        if (classroomDetails.getRoomNumber() != null &&
                !classroomDetails.getRoomNumber().equals(classroom.getRoomNumber()) &&
                classroomRepository.findByRoomNumber(classroomDetails.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("更新后的教室编号 '" + classroomDetails.getRoomNumber() + "' 已被其他教室使用！");
        }
        if (classroomDetails.getCapacity() != null && classroomDetails.getCapacity() <= 0) {
            throw new IllegalArgumentException("教室容量必须大于0！");
        }

        if (StringUtils.hasText(classroomDetails.getRoomNumber())) {
            classroom.setRoomNumber(classroomDetails.getRoomNumber());
        }
        if (classroomDetails.getCapacity() != null) {
            classroom.setCapacity(classroomDetails.getCapacity());
        }
        // 允许 type, buildingName, remarks 等设置为空字符串或null
        classroom.setType(classroomDetails.getType());
        classroom.setBuildingName(classroomDetails.getBuildingName());
        classroom.setFloorLevel(classroomDetails.getFloorLevel()); // 如果为null，则会更新为null
        classroom.setStatus(classroomDetails.getStatus()); // 应该有默认值，但允许更新
        classroom.setRemarks(classroomDetails.getRemarks());

        return classroomRepository.save(classroom);
    }

    @Override
    @Transactional
    public void deleteClassroom(Long id) {
        if (!classroomRepository.existsById(id)) {
            throw new IllegalArgumentException("未找到ID为 " + id + " 的教室，无法删除");
        }
        classroomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoomNumber(String roomNumber) {
        return classroomRepository.findByRoomNumber(roomNumber).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoomNumberAndIdNot(String roomNumber, Long id) {
        Optional<Classroom> existingClassroom = classroomRepository.findByRoomNumber(roomNumber);
        return existingClassroom.isPresent() && !existingClassroom.get().getId().equals(id);
    }
}