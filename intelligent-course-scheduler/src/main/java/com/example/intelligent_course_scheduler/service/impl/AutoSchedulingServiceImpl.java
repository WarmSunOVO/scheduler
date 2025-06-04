package com.example.intelligent_course_scheduler.service.impl;

// DTO 和 TimeSlot 等导入
import com.example.intelligent_course_scheduler.scheduling.ScheduledUnitDto;
import com.example.intelligent_course_scheduler.scheduling.ScheduleSolutionDto;
import com.example.intelligent_course_scheduler.scheduling.SchedulingException;
import com.example.intelligent_course_scheduler.scheduling.TimeSlot;

// 实体和仓库等导入
import com.example.intelligent_course_scheduler.entity.*;
import com.example.intelligent_course_scheduler.model.enums.ConstraintType;
import com.example.intelligent_course_scheduler.model.enums.TargetEntityType;
import com.example.intelligent_course_scheduler.exception.ResourceNotFoundException;
import com.example.intelligent_course_scheduler.repository.*;
import com.example.intelligent_course_scheduler.service.AutoSchedulingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AutoSchedulingServiceImpl implements AutoSchedulingService {

    private static final Logger logger = LoggerFactory.getLogger(AutoSchedulingServiceImpl.class);

    // 静态代码块检查日志级别
    static {
        if (logger.isTraceEnabled()) {
            System.out.println("--- TRACE logging is ENABLED for AutoSchedulingServiceImpl ---");
            logger.trace("Static block: TRACE logging check successful for AutoSchedulingServiceImpl.");
        } else if (logger.isDebugEnabled()) {
            System.out.println("--- DEBUG logging is ENABLED for AutoSchedulingServiceImpl, but TRACE is NOT. Some detailed logs might be missing. ---");
            logger.debug("Static block: DEBUG logging check successful, TRACE disabled for AutoSchedulingServiceImpl.");
        } else if (logger.isInfoEnabled()) {
            System.out.println("--- INFO logging is ENABLED for AutoSchedulingServiceImpl. Detailed TRACE/DEBUG logs will be MISSING. ---");
        } else {
            System.out.println("--- Logging level for AutoSchedulingServiceImpl is WARN or higher. Detailed TRACE/DEBUG logs will be MISSING. ---");
        }
    }

    private final TeachingTaskRepository teachingTaskRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final TeacherUnavailabilityRepository teacherUnavailabilityRepository;
    private final ClassGroupUnavailabilityRepository classGroupUnavailabilityRepository;
    private final RoomUnavailabilityRepository roomUnavailabilityRepository;
    private final ConstraintRuleRepository constraintRuleRepository;

    private Map<Long, List<TimeSlot>> teacherScheduleMap;
    private Map<String, List<TimeSlot>> classGroupScheduleMap;
    private Map<Long, List<TimeSlot>> roomScheduleMap;
    private Map<Long, Map<Integer, Integer>> teacherDailyPeriodsCount; // <TeacherId, <DayOfWeek, Count>>
    private List<ConstraintRule> activeHardConstraints;
    private Semester currentSemesterConfig;
    private List<Classroom> availableClassrooms;
    private List<TeachingTask> originalTasks;

    @Autowired
    public AutoSchedulingServiceImpl(TeachingTaskRepository teachingTaskRepository,
                                     SemesterRepository semesterRepository,
                                     TeacherRepository teacherRepository,
                                     ClassroomRepository classroomRepository,
                                     TeacherUnavailabilityRepository teacherUnavailabilityRepository,
                                     ClassGroupUnavailabilityRepository classGroupUnavailabilityRepository,
                                     RoomUnavailabilityRepository roomUnavailabilityRepository,
                                     ConstraintRuleRepository constraintRuleRepository) {
        this.teachingTaskRepository = teachingTaskRepository;
        this.semesterRepository = semesterRepository;
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
        this.teacherUnavailabilityRepository = teacherUnavailabilityRepository;
        this.classGroupUnavailabilityRepository = classGroupUnavailabilityRepository;
        this.roomUnavailabilityRepository = roomUnavailabilityRepository;
        this.constraintRuleRepository = constraintRuleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleSolutionDto generateScheduleForSemester(Long semesterId) throws SchedulingException {
        logger.info("排课请求开始: 学期ID {}", semesterId);
        initializeSchedulingState();

        currentSemesterConfig = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new SchedulingException("未找到学期配置: " + semesterId));
        logger.debug("加载学期配置: {} (ID:{}), 每周{}天, 每日{}节", currentSemesterConfig.getName(), semesterId, currentSemesterConfig.getDaysPerWeek(), currentSemesterConfig.getPeriodsPerDay());
        if (currentSemesterConfig.getDaysPerWeek() == null || currentSemesterConfig.getPeriodsPerDay() == null || currentSemesterConfig.getTotalWeeks() == null) {
            throw new SchedulingException("学期 " + currentSemesterConfig.getName() + " 的每周天数、每日节次或总周数未设置。");
        }

        originalTasks = teachingTaskRepository.findBySemesterId(semesterId);
        if (originalTasks.isEmpty()) {
            ScheduleSolutionDto solutionDto = new ScheduleSolutionDto();
            solutionDto.setSuccess(true);
            solutionDto.setMessage("没有需要安排的教学任务。");
            logger.info("学期ID {} 没有教学任务需要安排。", semesterId);
            return solutionDto;
        }
        logger.info("加载了 {} 个教学任务。", originalTasks.size());

        availableClassrooms = classroomRepository.findAllByStatus("可用");
        if (availableClassrooms.isEmpty()) {
            throw new SchedulingException("没有可用的教室用于排课。");
        }
        logger.info("加载了 {} 个可用教室。", availableClassrooms.size());

        loadAndPreprocessConstraints(semesterId);

        List<ScheduledUnitDto> unitsToSchedule = new ArrayList<>();
        for (TeachingTask task : originalTasks) {
            if (task.getSessionsPerWeek() == null || task.getSessionsPerWeek() <= 0 ||
                    task.getSessionLength() == null || task.getSessionLength() <= 0 ||
                    task.getTeacher() == null || task.getCourse() == null) {
                logger.warn("教学任务 (ID:{}) 的必要信息不完整 (周次数/连堂/教师/课程)，已跳过。", task.getId());
                continue;
            }
            for (int i = 0; i < task.getSessionsPerWeek(); i++) {
                ScheduledUnitDto unitDto = new ScheduledUnitDto();
                unitDto.setTeachingTaskId(task.getId());
                unitDto.setCourseName(task.getCourse().getName());
                unitDto.setTeacherName(task.getTeacher().getName());
                unitDto.setTargetGroup(task.getTargetGroup());
                unitsToSchedule.add(unitDto);
            }
        }
        logger.info("共创建 {} 个待排课程单元 (ScheduledUnitDto)。", unitsToSchedule.size());
        unitsToSchedule.forEach(u -> logger.trace("待排单元: TaskID {}, Course {}, Teacher {}", u.getTeachingTaskId(), u.getCourseName(), u.getTeacherName()));


        ScheduleSolutionDto solutionDto = new ScheduleSolutionDto();
        logger.info("开始回溯排课算法...");
        boolean success = backtrackScheduleAlgorithm(unitsToSchedule, 0, solutionDto);

        solutionDto.setSuccess(success);
        if (success) {
            solutionDto.setScheduledUnits(unitsToSchedule.stream()
                    .filter(dto -> dto.getDayOfWeek() != 0 && dto.getRoomId() != null)
                    .collect(Collectors.toList()));
            solutionDto.setMessage("课表成功生成！");
            logger.info("为学期ID {} 成功生成课表！", semesterId);
        } else {
            List<ScheduledUnitDto> scheduled = unitsToSchedule.stream()
                    .filter(dto -> dto.getDayOfWeek() != 0 && dto.getRoomId() != null)
                    .collect(Collectors.toList());
            solutionDto.setScheduledUnits(scheduled);

            List<String> unscheduledMessages = new ArrayList<>();
            for(ScheduledUnitDto dto : unitsToSchedule){
                if(dto.getDayOfWeek() == 0 || dto.getRoomId() == null){
                    unscheduledMessages.add(String.format("任务 %s (%s) 的一个单元未能安排。", dto.getCourseName(), dto.getTargetGroup()));
                }
            }
            if (solutionDto.getConflictMessages() == null) solutionDto.setConflictMessages(new ArrayList<>());
            solutionDto.getConflictMessages().addAll(unscheduledMessages);
            if (solutionDto.getConflictMessages().isEmpty() && !unitsToSchedule.isEmpty()){ // 如果没有具体冲突但又没成功
                solutionDto.getConflictMessages().add("未能为所有课程单元找到无冲突的安排。");
            }
            solutionDto.setMessage("排课失败或部分成功。");
            logger.warn("为学期ID {} 生成课表失败。", semesterId);
        }

        logger.debug("最终已安排的单元 (DTO): {}", solutionDto.getScheduledUnits());
        logger.warn("最终冲突/未安排信息: {}", solutionDto.getConflictMessages());
        logger.info("排课请求结束: 学期ID {}", semesterId);
        return solutionDto;
    }

    private void initializeSchedulingState() {
        teacherScheduleMap = new HashMap<>();
        classGroupScheduleMap = new HashMap<>();
        roomScheduleMap = new HashMap<>();
        teacherDailyPeriodsCount = new HashMap<>();
        activeHardConstraints = new ArrayList<>();
        currentSemesterConfig = null;
        availableClassrooms = new ArrayList<>();
        originalTasks = new ArrayList<>();
        logger.trace("排课内部状态已初始化。");
    }

    private void loadAndPreprocessConstraints(Long semesterId) {
        // (与之前版本一致)
        teacherUnavailabilityRepository.findBySemesterIdAndIsActiveTrue(semesterId).forEach(tu -> {
            if (tu.getTeacher() != null && tu.getTeacher().getId() != null) {
                teacherScheduleMap.computeIfAbsent(tu.getTeacher().getId(), k -> new ArrayList<>())
                        .add(new TimeSlot(tu.getDayOfWeek(), tu.getStartPeriod(), tu.getEndPeriod() - tu.getStartPeriod() + 1));
            }
        });
        classGroupUnavailabilityRepository.findBySemesterIdAndIsActiveTrue(semesterId).forEach(cu ->
                classGroupScheduleMap.computeIfAbsent(cu.getTargetGroup(), k -> new ArrayList<>())
                        .add(new TimeSlot(cu.getDayOfWeek(), cu.getStartPeriod(), cu.getEndPeriod() - cu.getStartPeriod() + 1))
        );
        roomUnavailabilityRepository.findBySemesterIdAndIsActiveTrue(semesterId).forEach(ru -> {
            if (ru.getClassroom() != null && ru.getClassroom().getId() != null) {
                roomScheduleMap.computeIfAbsent(ru.getClassroom().getId(), k -> new ArrayList<>())
                        .add(new TimeSlot(ru.getDayOfWeek(), ru.getStartPeriod(), ru.getEndPeriod() - ru.getStartPeriod() + 1));
            }
        });
        activeHardConstraints.addAll(constraintRuleRepository.findBySemesterIsNullAndIsActiveTrue().stream()
                .filter(rule -> rule.getConstraintType() == ConstraintType.HARD)
                .collect(Collectors.toList()));
        activeHardConstraints.addAll(constraintRuleRepository.findBySemesterIdAndIsActiveTrue(semesterId).stream()
                .filter(rule -> rule.getConstraintType() == ConstraintType.HARD)
                .collect(Collectors.toList()));
        logger.info("预填充和加载了 {} 条教师不可用, {} 条班级不可用, {} 条教室不可用, {} 条通用硬约束。",
                teacherScheduleMap.values().stream().mapToInt(List::size).sum(),
                classGroupScheduleMap.values().stream().mapToInt(List::size).sum(),
                roomScheduleMap.values().stream().mapToInt(List::size).sum(),
                activeHardConstraints.size()
        );
        activeHardConstraints.forEach(r -> logger.trace("加载硬约束: {} (ID:{}), Code: {}, Target: {}/{}, Params: {}", r.getName(), r.getId(), r.getConstraintCode(), r.getTargetEntityType(), r.getTargetEntityId(), r.getParametersJson()));
    }

    private boolean backtrackScheduleAlgorithm(List<ScheduledUnitDto> unitsToSchedule, int unitIndex, ScheduleSolutionDto solutionDto) {
        logger.trace("进入回溯: unitIndex = {}, totalUnits = {}", unitIndex, unitsToSchedule.size());
        if (unitIndex == unitsToSchedule.size()) {
            logger.trace("所有单元已成功安排!");
            return true;
        }

        ScheduledUnitDto currentUnitDto = unitsToSchedule.get(unitIndex);
        TeachingTask task = originalTasks.stream()
                .filter(t -> t.getId().equals(currentUnitDto.getTeachingTaskId()))
                .findFirst()
                .orElseThrow(() -> new SchedulingException("算法内部错误: 找不到教学任务实体 for DTO with TaskID: " + currentUnitDto.getTeachingTaskId()));
        int sessionLength = task.getSessionLength();
        logger.trace("尝试安排单元: TaskID {}, Course '{}', Teacher '{}', Group '{}', Length {}",
                task.getId(), task.getCourse().getName(), task.getTeacher().getName(), task.getTargetGroup(), sessionLength);

        for (int day = 1; day <= currentSemesterConfig.getDaysPerWeek(); day++) {
            logger.trace("  尝试星期: {}", day);
            for (int startPeriod = 1; startPeriod <= currentSemesterConfig.getPeriodsPerDay() - sessionLength + 1; startPeriod++) {
                TimeSlot potentialSlot = new TimeSlot(day, startPeriod, sessionLength);
                logger.trace("    尝试时间槽: {}", potentialSlot);

                if (availableClassrooms.isEmpty()) {
                    logger.warn("      没有可用教室可供尝试! Unit: {}, Slot: {}", currentUnitDto.getTeachingTaskId(), potentialSlot);
                    continue;
                }

                for (Classroom room : availableClassrooms) {
                    logger.trace("      尝试教室: {} (ID: {}, Type: {})", room.getRoomNumber(), room.getId(), room.getType());

                    if (!isRoomTypeMatch(room.getType(), task.getRequiredRoomType())) {
                        logger.trace("        教室类型不匹配. 要求: '{}', 教室实际: '{}'. 跳过.", task.getRequiredRoomType(), room.getType());
                        continue;
                    }
                    logger.trace("        教室类型匹配.");

                    if (checkAllHardConstraints(task, potentialSlot, room.getId(), solutionDto)) {
                        logger.trace("        硬约束检查通过! 分配单元 {} 到时间 {} 教室 {}", task.getId(), potentialSlot, room.getRoomNumber());
                        currentUnitDto.setDayOfWeek(potentialSlot.getDayOfWeek());
                        currentUnitDto.setStartPeriod(potentialSlot.getStartPeriod());
                        currentUnitDto.setEndPeriod(potentialSlot.getEndPeriod());
                        currentUnitDto.setRoomId(room.getId());
                        currentUnitDto.setRoomNumber(room.getRoomNumber());

                        addOccupancy(task.getTeacher().getId(), task.getTargetGroup(), room.getId(), potentialSlot);

                        if (backtrackScheduleAlgorithm(unitsToSchedule, unitIndex + 1, solutionDto)) {
                            return true;
                        }

                        logger.trace("        回溯: 撤销单元 {} 从时间 {} 教室 {}", task.getId(), potentialSlot, room.getRoomNumber());
                        removeOccupancy(task.getTeacher().getId(), task.getTargetGroup(), room.getId(), potentialSlot);
                        currentUnitDto.setDayOfWeek(0);
                        currentUnitDto.setStartPeriod(0);
                        currentUnitDto.setEndPeriod(0);
                        currentUnitDto.setRoomId(null);
                        currentUnitDto.setRoomNumber(null);
                    } else {
                        logger.trace("        硬约束检查失败 for unit {} at slot {} in room {}.", task.getId(), potentialSlot, room.getRoomNumber());
                    }
                }
            }
        }
        logger.warn("单元 (TaskID {}) 无法找到任何合适位置.", task.getId());
        // solutionDto.getConflictMessages().add(String.format("单元 (任务ID %d, 课程 %s) 无法找到位置", task.getId(), task.getCourse().getName()));
        return false;
    }

    private boolean checkAllHardConstraints(TeachingTask task, TimeSlot slot, Long roomId, ScheduleSolutionDto solutionDto) {
        logger.trace("      检查硬约束 for TaskID {}, Slot {}, RoomID {}", task.getId(), slot, roomId);
        String teacherName = task.getTeacher().getName();
        String targetGroup = task.getTargetGroup();
        Optional<Classroom> classroomOpt = availableClassrooms.stream().filter(c -> c.getId().equals(roomId)).findFirst();
        String roomIdentifier = classroomOpt.map(Classroom::getRoomNumber).orElse("ID:" + roomId);


        if (isTimeSlotOccupied(teacherScheduleMap.get(task.getTeacher().getId()), slot)) {
            logger.trace("        约束冲突: 教师 '{}' 在时间 {} 已被占用.", teacherName, slot);
            return false;
        }
        if (isTimeSlotOccupied(classGroupScheduleMap.get(targetGroup), slot)) {
            logger.trace("        约束冲突: 班级 '{}' 在时间 {} 已被占用.", targetGroup, slot);
            return false;
        }
        if (isTimeSlotOccupied(roomScheduleMap.get(roomId), slot)) {
            logger.trace("        约束冲突: 教室 '{}' 在时间 {} 已被占用.", roomIdentifier, slot);
            return false;
        }
        logger.trace("        基础时间占用检查通过.");

        for (ConstraintRule rule : activeHardConstraints) {
            logger.trace("        评估通用硬约束: '{}' (ID:{}, Code:{})", rule.getName(), rule.getId(), rule.getConstraintCode());
            if (!evaluateGenericHardConstraint(rule, task, slot, roomId, solutionDto)) {
                logger.trace("        通用硬约束 '{}' (ID:{}) 未通过!", rule.getName(), rule.getId());
                return false;
            }
            logger.trace("        通用硬约束 '{}' (ID:{}) 通过.", rule.getName(), rule.getId());
        }
        logger.trace("      所有硬约束检查通过 for TaskID {}, Slot {}, RoomID {}", task.getId(), slot, roomId);
        return true;
    }

    private boolean evaluateGenericHardConstraint(ConstraintRule rule, TeachingTask task, TimeSlot slot, Long roomId, ScheduleSolutionDto solutionDto) {
        Map<String, Object> params = rule.getParametersJson();
        if (params == null) params = Collections.emptyMap();
        boolean ruleApplies = false;
        if (rule.getTargetEntityType() == TargetEntityType.GLOBAL) ruleApplies = true;
        else if (rule.getTargetEntityType() == TargetEntityType.TEACHER && rule.getTargetEntityId().equals(task.getTeacher().getId())) ruleApplies = true;
        else if (rule.getTargetEntityType() == TargetEntityType.COURSE && rule.getTargetEntityId().equals(task.getCourse().getId())) ruleApplies = true;
        else if (rule.getTargetEntityType() == TargetEntityType.ROOM && rule.getTargetEntityId().equals(roomId)) ruleApplies = true;
        else if (rule.getTargetEntityType() == TargetEntityType.TEACHING_TASK && rule.getTargetEntityId().equals(task.getId())) ruleApplies = true;
        else if (rule.getTargetEntityType() == TargetEntityType.CLASS_GROUP) {
            if (params.containsKey("targetGroup") && params.get("targetGroup").equals(task.getTargetGroup())) ruleApplies = true;
        }

        if (!ruleApplies) {
            logger.trace("          规则 '{}' 不适用于当前单元 (TaskID {}).", rule.getName(), task.getId());
            return true;
        }
        logger.trace("          规则 '{}' 适用于当前单元, 开始评估...", rule.getName());


        switch (rule.getConstraintCode()) {
            case "COURSE_FIXED_SCHEDULE":
                Integer day = 安全转换ToInt(params.get("dayOfWeek"));
                Integer start = 安全转换ToInt(params.get("startPeriod"));
                int sessionLength = task.getSessionLength(); // 默认用任务自身的连读
                if(params.containsKey("sessionLength")) { // 约束可覆盖任务的连读
                    Integer paramLength = 安全转换ToInt(params.get("sessionLength"));
                    if(paramLength != null && paramLength > 0) sessionLength = paramLength;
                }
                Integer end = params.containsKey("endPeriod") ? 安全转换ToInt(params.get("endPeriod")) : (start != null ? start + sessionLength - 1 : null);
                Long fixedRoomId = params.containsKey("roomId") ? 安全转换ToLong(params.get("roomId")) : null;

                if (day == null || start == null || end == null ) {
                    logger.warn("          COURSE_FIXED_SCHEDULE 规则 {} ({}) 参数不完整. 跳过.", rule.getName(), rule.getId());
                    return true;
                }
                TimeSlot fixedSlot = new TimeSlot(day, start, end - start + 1);

                if (!slot.equals(fixedSlot)) {
                    logger.trace("            COURSE_FIXED_SCHEDULE 冲突: 尝试时间 {} 与固定时间 {} 不符.", slot, fixedSlot);
                    return false;
                }
                if (fixedRoomId != null && !roomId.equals(fixedRoomId)) {
                    logger.trace("            COURSE_FIXED_SCHEDULE 冲突: 尝试教室ID {} 与固定教室ID {} 不符.", roomId, fixedRoomId);
                    return false;
                }
                logger.trace("            COURSE_FIXED_SCHEDULE 通过.");
                break;

            case "GLOBAL_NO_FRIDAY_AFTERNOON":
                if (slot.getDayOfWeek() == 5) { // 假设周五是5
                    Integer afternoonStart = 安全转换ToInt(params.get("afternoonStartPeriod"));
                    if (afternoonStart == null) afternoonStart = (currentSemesterConfig.getPeriodsPerDay() / 2) + 1;
                    if (slot.getStartPeriod() >= afternoonStart) {
                        logger.trace("            GLOBAL_NO_FRIDAY_AFTERNOON 冲突: 尝试时间 {} 在周五下午禁用时段.", slot);
                        return false;
                    }
                }
                logger.trace("            GLOBAL_NO_FRIDAY_AFTERNOON 通过 (或不适用).");
                break;

            case "GLOBAL_NO_WEDNESDAY":
                if (slot.getDayOfWeek() == 3) { // 周三是3
                    logger.trace("            GLOBAL_NO_WEDNESDAY 冲突: 尝试时间 {} 在周三禁用时段.", slot);
                    return false;
                }
                logger.trace("            GLOBAL_NO_WEDNESDAY 通过 (或不适用).");
                break;

            case "TEACHER_MAX_DAILY_PERIODS":
                if (rule.getTargetEntityType() == TargetEntityType.TEACHER && rule.getTargetEntityId().equals(task.getTeacher().getId())) {
                    Integer maxPeriods = 安全转换ToInt(params.get("maxPeriods"));
                    if (maxPeriods == null || maxPeriods <= 0) {
                        logger.warn("          TEACHER_MAX_DAILY_PERIODS 规则 {} ({}) maxPeriods 参数无效. 跳过.", rule.getName(), rule.getId());
                        return true;
                    }
                    int currentDay = slot.getDayOfWeek();
                    int periodsForThisUnit = slot.getEndPeriod() - slot.getStartPeriod() + 1;
                    int alreadyScheduledPeriodsToday = teacherDailyPeriodsCount
                            .getOrDefault(task.getTeacher().getId(), Collections.emptyMap())
                            .getOrDefault(currentDay, 0);
                    if (alreadyScheduledPeriodsToday + periodsForThisUnit > maxPeriods) {
                        logger.trace("            TEACHER_MAX_DAILY_PERIODS 冲突: 教师 {} 在周 {} 已排 {} + 尝试排 {} = {} 节, 超过最大 {} 节.",
                                task.getTeacher().getName(), currentDay, alreadyScheduledPeriodsToday, periodsForThisUnit, alreadyScheduledPeriodsToday + periodsForThisUnit, maxPeriods);
                        return false;
                    }
                    logger.trace("            TEACHER_MAX_DAILY_PERIODS 通过: 教师 {} 在周 {} 当前已排 {} + 尝试排 {} = {} 节, 未超过最大 {} 节.",
                            task.getTeacher().getName(), currentDay, alreadyScheduledPeriodsToday, periodsForThisUnit, alreadyScheduledPeriodsToday + periodsForThisUnit, maxPeriods);
                }
                break;

            default:
                logger.warn("          未实现的硬约束代码评估 (default case): {} (规则ID: {})", rule.getConstraintCode(), rule.getId());
                break;
        }
        return true;
    }

    // --- 辅助方法 (保持不变) ---
    private boolean isRoomTypeMatch(String roomType, String requiredRoomType) { return (requiredRoomType == null || requiredRoomType.trim().isEmpty() || "无特殊要求".equalsIgnoreCase(requiredRoomType.trim())) || (requiredRoomType.equalsIgnoreCase(roomType != null ? roomType.trim() : "")); }
    private void addOccupancy(Long teacherId, String targetGroup, Long roomId, TimeSlot slot) {
        teacherScheduleMap.computeIfAbsent(teacherId, k -> new ArrayList<>()).add(slot);
        classGroupScheduleMap.computeIfAbsent(targetGroup, k -> new ArrayList<>()).add(slot);
        roomScheduleMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(slot);
        teacherDailyPeriodsCount.computeIfAbsent(teacherId, k -> new HashMap<>())
                .merge(slot.getDayOfWeek(), slot.getEndPeriod() - slot.getStartPeriod() + 1, Integer::sum);
    }
    private void removeOccupancy(Long teacherId, String targetGroup, Long roomId, TimeSlot slot) {
        teacherScheduleMap.getOrDefault(teacherId, Collections.emptyList()).remove(slot);
        classGroupScheduleMap.getOrDefault(targetGroup, Collections.emptyList()).remove(slot);
        roomScheduleMap.getOrDefault(roomId, Collections.emptyList()).remove(slot);
        Map<Integer, Integer> dailyCounts = teacherDailyPeriodsCount.get(teacherId);
        if (dailyCounts != null && dailyCounts.containsKey(slot.getDayOfWeek())) {
            int newCount = dailyCounts.get(slot.getDayOfWeek()) - (slot.getEndPeriod() - slot.getStartPeriod() + 1);
            if (newCount <= 0) { dailyCounts.remove(slot.getDayOfWeek()); }
            else { dailyCounts.put(slot.getDayOfWeek(), newCount); }
        }
    }
    private boolean isTimeSlotOccupied(List<TimeSlot> existingSlots, TimeSlot newSlot) { if (existingSlots == null) return false; for (TimeSlot existing : existingSlots) { if (existing.overlaps(newSlot)) return true; } return false; }
    private Integer 安全转换ToInt(Object obj) { if (obj instanceof Number) { return ((Number)obj).intValue(); } if (obj instanceof String) { try { return Integer.parseInt((String)obj); } catch (NumberFormatException e) {logger.warn("安全转换ToInt失败: '{}' 不是一个有效的整数字符串。", obj, e); return null;} } if (obj != null) {logger.warn("安全转换ToInt失败: 对象类型 {} ('{}') 不是Number或String。", obj.getClass().getName(), obj);} return null; }
    private Long 安全转换ToLong(Object obj) { if (obj instanceof Number) { return ((Number)obj).longValue(); } if (obj instanceof String) { try { return Long.parseLong((String)obj); } catch (NumberFormatException e) {logger.warn("安全转换ToLong失败: '{}' 不是一个有效的Long字符串。", obj, e);return null;} } if (obj != null) {logger.warn("安全转换ToLong失败: 对象类型 {} ('{}') 不是Number或String。", obj.getClass().getName(), obj);} return null; }
}