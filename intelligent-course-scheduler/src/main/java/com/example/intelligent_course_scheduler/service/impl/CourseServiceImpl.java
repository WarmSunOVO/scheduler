package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.Course;
import com.example.intelligent_course_scheduler.payload.CourseDto;
import com.example.intelligent_course_scheduler.repository.CourseRepository;
import com.example.intelligent_course_scheduler.service.CourseService;
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
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    @Transactional
    public CourseDto createCourse(CourseDto courseDto) {
        if (courseRepository.existsByCourseCode(courseDto.getCourseCode())) {
            throw new IllegalArgumentException("错误：课程代码 '" + courseDto.getCourseCode() + "' 已存在！");
        }
        Course course = courseDto.toEntity();
        if (!StringUtils.hasText(course.getStatus())) { // Service层确保默认值
            course.setStatus("激活");
        }
        return CourseDto.fromEntity(courseRepository.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseDto> getCourseById(Long id) {
        return courseRepository.findById(id).map(CourseDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDto> getAllCourses(Pageable pageable, String query) {
        Specification<Course> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(query)) {
                String searchQuery = "%" + query.toLowerCase() + "%";
                Predicate codeLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("courseCode")), searchQuery);
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchQuery);
                Predicate typeLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("type")), searchQuery);
                Predicate departmentLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("departmentName")), searchQuery);
                predicates.add(criteriaBuilder.or(codeLike, nameLike, typeLike, departmentLike));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return courseRepository.findAll(spec, pageable).map(CourseDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getAllCoursesList() {
        return courseRepository.findAll(Sort.by(Sort.Direction.ASC, "name")) // 按名称升序
                .stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错误：找不到ID为 " + id + " 的课程"));

        if (!existingCourse.getCourseCode().equals(courseDto.getCourseCode()) &&
                courseRepository.existsByCourseCode(courseDto.getCourseCode())) {
            throw new IllegalArgumentException("错误：更新后的课程代码 '" + courseDto.getCourseCode() + "' 已被其他课程使用！");
        }

        existingCourse.setCourseCode(courseDto.getCourseCode());
        existingCourse.setName(courseDto.getName());
        existingCourse.setCredits(courseDto.getCredits());
        existingCourse.setHoursPerWeek(courseDto.getHoursPerWeek());
        existingCourse.setTotalHours(courseDto.getTotalHours());
        existingCourse.setType(courseDto.getType());
        existingCourse.setRequiredRoomType(courseDto.getRequiredRoomType());
        existingCourse.setDepartmentName(courseDto.getDepartmentName());
        existingCourse.setDescription(courseDto.getDescription());
        existingCourse.setStatus(StringUtils.hasText(courseDto.getStatus()) ? courseDto.getStatus() : "激活");

        return CourseDto.fromEntity(courseRepository.save(existingCourse));
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("错误：找不到ID为 " + id + " 的课程，无法删除");
        }
        // TODO: 实际项目中，删除课程前应检查是否有教学任务等关联数据
        courseRepository.deleteById(id);
    }
}