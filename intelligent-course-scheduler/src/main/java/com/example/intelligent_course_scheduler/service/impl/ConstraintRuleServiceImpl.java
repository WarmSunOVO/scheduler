package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.ConstraintRule;
import com.example.intelligent_course_scheduler.entity.Semester;
import com.example.intelligent_course_scheduler.exception.ResourceNotFoundException;
import com.example.intelligent_course_scheduler.model.enums.TargetEntityType; // 确保 import
import com.example.intelligent_course_scheduler.payload.ConstraintRuleRequestDto;
import com.example.intelligent_course_scheduler.payload.ConstraintRuleResponseDto;
import com.example.intelligent_course_scheduler.repository.ConstraintRuleRepository;
import com.example.intelligent_course_scheduler.repository.SemesterRepository;
import com.example.intelligent_course_scheduler.repository.TeacherRepository;
import com.example.intelligent_course_scheduler.repository.CourseRepository;
import com.example.intelligent_course_scheduler.repository.ClassroomRepository;
// import com.example.intelligent_course_scheduler.repository.TeachingTaskRepository; // 如果需要校验教学任务
import com.example.intelligent_course_scheduler.service.ConstraintRuleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConstraintRuleServiceImpl implements ConstraintRuleService {

    private final ConstraintRuleRepository constraintRuleRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    // private final TeachingTaskRepository teachingTaskRepository;


    @Autowired
    public ConstraintRuleServiceImpl(ConstraintRuleRepository constraintRuleRepository,
                                     SemesterRepository semesterRepository,
                                     TeacherRepository teacherRepository,
                                     CourseRepository courseRepository,
                                     ClassroomRepository classroomRepository
            /*, TeachingTaskRepository teachingTaskRepository */) {
        this.constraintRuleRepository = constraintRuleRepository;
        this.semesterRepository = semesterRepository;
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        // this.teachingTaskRepository = teachingTaskRepository;
    }

    @Override
    public ConstraintRuleResponseDto createConstraintRule(ConstraintRuleRequestDto requestDto) {
        Semester semester = null;
        if (requestDto.getSemesterId() != null) {
            semester = semesterRepository.findById(requestDto.getSemesterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", requestDto.getSemesterId()));
        }

        validateTargetEntity(requestDto.getTargetEntityType(), requestDto.getTargetEntityId());
        // 初期：对 parametersJson 的校验可以简化或暂缓
        // validateParametersJson(requestDto.getConstraintCode(), requestDto.getParametersJson());

        ConstraintRule rule = new ConstraintRule();
        mapDtoToEntity(requestDto, rule, semester);

        ConstraintRule savedRule = constraintRuleRepository.save(rule);
        return mapEntityToDto(savedRule);
    }

    @Override
    public ConstraintRuleResponseDto getConstraintRuleById(Long id) {
        ConstraintRule rule = constraintRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConstraintRule", "id", id));
        return mapEntityToDto(rule);
    }

    @Override
    public Page<ConstraintRuleResponseDto> getAllConstraintRules(Pageable pageable, Map<String, String> searchParams) {
        Specification<ConstraintRule> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchParams != null) {
                searchParams.forEach((key, value) -> {
                    if (value != null && !value.trim().isEmpty()) {
                        try { // 添加 try-catch 以处理无效的枚举值或数字格式
                            switch (key) {
                                case "name":
                                case "constraintCode":
                                    predicates.add(criteriaBuilder.like(root.get(key), "%" + value + "%"));
                                    break;
                                case "constraintType":
                                    predicates.add(criteriaBuilder.equal(root.get(key), com.example.intelligent_course_scheduler.model.enums.ConstraintType.valueOf(value.toUpperCase())));
                                    break;
                                case "targetEntityType":
                                    predicates.add(criteriaBuilder.equal(root.get(key), com.example.intelligent_course_scheduler.model.enums.TargetEntityType.valueOf(value.toUpperCase())));
                                    break;
                                case "isActive":
                                    predicates.add(criteriaBuilder.equal(root.get(key), Boolean.valueOf(value)));
                                    break;
                                case "semesterId":
                                    if ("null".equalsIgnoreCase(value)) { // 允许查询全局约束
                                        predicates.add(criteriaBuilder.isNull(root.get("semester")));
                                    } else {
                                        predicates.add(criteriaBuilder.equal(root.get("semester").get("id"), Long.valueOf(value)));
                                    }
                                    break;
                                case "targetEntityId":
                                    predicates.add(criteriaBuilder.equal(root.get(key), Long.valueOf(value)));
                                    break;
                            }
                        } catch (IllegalArgumentException e) {
                            // Log or handle invalid search param value (e.g., bad enum string)
                            System.err.println("Invalid search parameter value for key '" + key + "': " + value);
                        }
                    }
                });
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<ConstraintRule> rulePage = constraintRuleRepository.findAll(spec, pageable);
        return rulePage.map(this::mapEntityToDto);
    }

    @Override
    public ConstraintRuleResponseDto updateConstraintRule(Long id, ConstraintRuleRequestDto requestDto) {
        ConstraintRule rule = constraintRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConstraintRule", "id", id));

        Semester semester = null;
        if (requestDto.getSemesterId() != null) {
            semester = semesterRepository.findById(requestDto.getSemesterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", requestDto.getSemesterId()));
        } else if (rule.getSemester() != null && requestDto.getTargetEntityType() != TargetEntityType.GLOBAL) {
            // 如果DTO中semesterId为null，但不是全局规则，且数据库中原来有关联学期，
            // 保持原样，或者根据业务逻辑决定是否清除学期关联。
            // 这里我们假设如果DTO中semesterId为null，则表示要清除学期关联（如果业务允许）或不更新。
            // 为了简化，如果dto中为null，则设置为null。若业务不允许，则应在此处校验。
            // semester = null; // 这样会将学期关联清除，如果DTO中为null
        }


        validateTargetEntity(requestDto.getTargetEntityType(), requestDto.getTargetEntityId());
        // validateParametersJson(requestDto.getConstraintCode(), requestDto.getParametersJson());

        mapDtoToEntity(requestDto, rule, semester);
        if (requestDto.getSemesterId() == null && requestDto.getTargetEntityType() != TargetEntityType.GLOBAL) {
            // 如果dto中semesterId为null，但不是全局规则，则不应该将实体的semester设为null
            // 或者说，如果业务上允许清除学期，那么上面的 mapDtoToEntity(..., semester) 中 semester 为 null 即可
            // 如果不允许，则需要在此处抛异常或从 rule 中获取原 semester
            // 为了简化，我们允许更新时将 semesterId 置为 null (如果数据库列允许)
        } else {
            rule.setSemester(semester); // 确保在 mapDtoToEntity 之后，如果 semester 有值，则设置
        }


        ConstraintRule updatedRule = constraintRuleRepository.save(rule);
        return mapEntityToDto(updatedRule);
    }

    @Override
    public void deleteConstraintRule(Long id) {
        if (!constraintRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("ConstraintRule", "id", id);
        }
        constraintRuleRepository.deleteById(id);
    }

    @Override
    public List<ConstraintRuleResponseDto> getActiveRulesForScheduling(Long semesterId) {
        semesterRepository.findById(semesterId) // 校验学期是否存在
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", semesterId));

        List<ConstraintRule> globalRules = constraintRuleRepository.findBySemesterIsNullAndIsActiveTrue();
        List<ConstraintRule> semesterSpecificRules = constraintRuleRepository.findBySemesterIdAndIsActiveTrue(semesterId);

        List<ConstraintRule> allRules = new ArrayList<>(globalRules);
        allRules.addAll(semesterSpecificRules);

        return allRules.stream().distinct().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    private void mapDtoToEntity(ConstraintRuleRequestDto dto, ConstraintRule entity, Semester semester) {
        // BeanUtils.copyProperties(dto, entity, "id", "createdAt", "updatedAt", "semester"); // 避免复制这些
        entity.setName(dto.getName());
        entity.setConstraintCode(dto.getConstraintCode());
        entity.setConstraintType(dto.getConstraintType());
        entity.setTargetEntityType(dto.getTargetEntityType());
        entity.setTargetEntityId(dto.getTargetEntityId());
        entity.setDescription(dto.getDescription());
        entity.setParametersJson(dto.getParametersJson());
        entity.setPenaltyWeight(dto.getPenaltyWeight());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        } else if (entity.getId() == null) {
            entity.setIsActive(true);
        }
        entity.setSemester(semester); // 设置学期
    }

    private ConstraintRuleResponseDto mapEntityToDto(ConstraintRule rule) {
        ConstraintRuleResponseDto dto = new ConstraintRuleResponseDto();
        BeanUtils.copyProperties(rule, dto, "semester");
        if (rule.getSemester() != null) {
            dto.setSemesterId(rule.getSemester().getId());
            dto.setSemesterDisplay(rule.getSemester().getAcademicYear() + " " + rule.getSemester().getName());
        }
        // 初期不填充 targetEntityDisplay
        // dto.setTargetEntityDisplay(fetchTargetEntityDisplay(rule.getTargetEntityType(), rule.getTargetEntityId()));
        return dto;
    }

    private void validateTargetEntity(TargetEntityType entityType, Long entityId) {
        if (entityType != TargetEntityType.GLOBAL && entityId == null) {
            throw new IllegalArgumentException("当目标实体类型不为GLOBAL时，目标实体ID不能为空: " + entityType);
        }
        if (entityType == TargetEntityType.GLOBAL && entityId != null) {
            throw new IllegalArgumentException("当目标实体类型为GLOBAL时，目标实体ID必须为空。");
        }

        if (entityId != null) {
            boolean exists = false;
            String resourceName = "";
            switch (entityType) {
                case TEACHER:
                    exists = teacherRepository.existsById(entityId);
                    resourceName = "Teacher";
                    break;
                case COURSE:
                    exists = courseRepository.existsById(entityId);
                    resourceName = "Course";
                    break;
                case ROOM:
                    exists = classroomRepository.existsById(entityId);
                    resourceName = "Classroom";
                    break;
                case TEACHING_TASK:
                    // exists = teachingTaskRepository.existsById(entityId); // 假设有
                    // resourceName = "TeachingTask";
                    // 对于 TEACHING_TASK, CLASS_GROUP (如果其ID是数字型)，也需要相应的Repository
                    break;
                default: // GLOBAL or CLASS_GROUP (if targetEntityId is not used for it)
                    return; // 不需要校验ID存在性
            }
            if (!exists) {
                throw new ResourceNotFoundException(resourceName, "id", entityId);
            }
        }
    }

    // private void validateParametersJson(String constraintCode, Map<String, Object> params) {
    //     // TODO: 根据 constraintCode 实现对 params 内部结构的校验
    //     // 例如：if ("TEACHER_TIME_PREFERENCE".equals(constraintCode)) {
    //     //           if (!params.containsKey("dayOfWeek") || !params.containsKey("preferenceScore")) {
    //     //               throw new IllegalArgumentException("TEACHER_TIME_PREFERENCE 缺少必要参数 dayOfWeek 或 preferenceScore");
    //     //           }
    //     //       }
    // }
}