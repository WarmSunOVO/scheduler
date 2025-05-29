package com.example.intelligent_course_scheduler.service; // 替换为你的包名

import com.example.intelligent_course_scheduler.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ClassroomService {

    // 创建新的教室
    Classroom createClassroom(Classroom classroom);

    // 根据ID获取教室信息
    Optional<Classroom> getClassroomById(Long id);

    // 根据教室编号获取教室信息
    Optional<Classroom> getClassroomByRoomNumber(String roomNumber);

    // 获取所有教室列表
    List<Classroom> getAllClassrooms();

    // 分页获取教室列表 (后续可以加入查询条件)
    Page<Classroom> getClassrooms(String roomNumber, String type, String status, Pageable pageable);

    // 更新教室信息
    Classroom updateClassroom(Long id, Classroom classroomDetails);

    // 删除教室
    void deleteClassroom(Long id);

    // 检查教室编号是否已存在 (用于创建和更新时的校验)
    boolean existsByRoomNumber(String roomNumber);

    // 检查教室编号是否已存在 (排除指定ID的教室，用于更新校验)
    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);
}