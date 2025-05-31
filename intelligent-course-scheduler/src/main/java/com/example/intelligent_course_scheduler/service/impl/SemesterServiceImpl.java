package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.Semester;
import com.example.intelligent_course_scheduler.payload.SemesterDto;
import com.example.intelligent_course_scheduler.repository.SemesterRepository;
import com.example.intelligent_course_scheduler.service.SemesterService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SemesterServiceImpl implements SemesterService {

    @Autowired
    private SemesterRepository semesterRepository;

    @Override
    @Transactional
    public SemesterDto createSemester(SemesterDto semesterDto) {
        if (semesterRepository.existsByAcademicYearAndName(semesterDto.getAcademicYear(), semesterDto.getName())) {
            throw new IllegalArgumentException("错误：学年 '" + semesterDto.getAcademicYear() + "' 的 '" + semesterDto.getName() + "' 已存在！");
        }
        // 如果 isCurrent 为 true，需要将其他学期的 isCurrent 设置为 false
        if (Boolean.TRUE.equals(semesterDto.getIsCurrent())) {
            setOnlyOneCurrentSemester(null); // 将所有其他设为非当前
        }
        Semester semester = semesterDto.toEntity();
        return SemesterDto.fromEntity(semesterRepository.save(semester));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SemesterDto> getSemesterById(Long id) {
        return semesterRepository.findById(id).map(SemesterDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SemesterDto> getAllSemesters(Pageable pageable, String query) {
        Specification<Semester> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(query)) {
                String searchQuery = "%" + query.toLowerCase() + "%";
                Predicate academicYearLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("academicYear")), searchQuery);
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchQuery);
                predicates.add(criteriaBuilder.or(academicYearLike, nameLike));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return semesterRepository.findAll(spec, pageable).map(SemesterDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SemesterDto> getAllSemestersList() {
        return semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "academicYear").and(Sort.by(Sort.Direction.ASC, "name")))
                .stream()
                .map(SemesterDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public SemesterDto updateSemester(Long id, SemesterDto semesterDto) {
        Semester existingSemester = semesterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错误：找不到ID为 " + id + " 的学期"));

        // 检查更新后的学年和名称组合是否与已存在的其他学期冲突
        if (!existingSemester.getAcademicYear().equals(semesterDto.getAcademicYear()) ||
                !existingSemester.getName().equals(semesterDto.getName())) {
            if (semesterRepository.existsByAcademicYearAndName(semesterDto.getAcademicYear(), semesterDto.getName())) {
                Optional<Semester> conflictingSemester = semesterRepository.findByAcademicYearAndName(semesterDto.getAcademicYear(), semesterDto.getName());
                if (conflictingSemester.isPresent() && !conflictingSemester.get().getId().equals(id)) {
                    throw new IllegalArgumentException("错误：学年 '" + semesterDto.getAcademicYear() + "' 的 '" + semesterDto.getName() + "' 已被其他学期使用！");
                }
            }
        }

        // 如果将此学期设为当前学期，则将其他学期设为非当前
        if (Boolean.TRUE.equals(semesterDto.getIsCurrent()) && (existingSemester.getIsCurrent() == null || !existingSemester.getIsCurrent())) {
            setOnlyOneCurrentSemester(id);
        }


        existingSemester.setAcademicYear(semesterDto.getAcademicYear());
        existingSemester.setName(semesterDto.getName());
        existingSemester.setStartDate(semesterDto.getStartDate());
        existingSemester.setEndDate(semesterDto.getEndDate());
        existingSemester.setIsCurrent(semesterDto.getIsCurrent() != null ? semesterDto.getIsCurrent() : false);
        existingSemester.setRemarks(semesterDto.getRemarks());

        return SemesterDto.fromEntity(semesterRepository.save(existingSemester));
    }

    private void setOnlyOneCurrentSemester(Long currentSemesterIdToKeep) {
        List<Semester> currentSemesters = semesterRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsCurrent()))
                .collect(Collectors.toList());

        for (Semester sem : currentSemesters) {
            if (currentSemesterIdToKeep == null || !sem.getId().equals(currentSemesterIdToKeep)) {
                sem.setIsCurrent(false);
                semesterRepository.save(sem);
            }
        }
    }


    @Override
    @Transactional
    public void deleteSemester(Long id) {
        if (!semesterRepository.existsById(id)) {
            throw new RuntimeException("错误：找不到ID为 " + id + " 的学期，无法删除");
        }
        // TODO: 实际项目中，删除学期前应检查是否有教学任务等关联数据
        semesterRepository.deleteById(id);
    }
}