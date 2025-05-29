package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.Teacher;
import com.example.intelligent_course_scheduler.repository.TeacherRepository;
import com.example.intelligent_course_scheduler.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 用于检查字符串是否为空

import java.util.List;
import java.util.Optional;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    @Transactional
    public Teacher createTeacher(Teacher teacher) {
        if (teacherRepository.existsByTeacherIdNumber(teacher.getTeacherIdNumber())) {
            throw new IllegalArgumentException("教师工号 '" + teacher.getTeacherIdNumber() + "' 已存在！");
        }
        if (StringUtils.hasText(teacher.getEmail()) && teacherRepository.existsByEmail(teacher.getEmail())) {
            throw new IllegalArgumentException("电子邮箱 '" + teacher.getEmail() + "' 已被使用！");
        }
        // 可在此处添加更多校验，如姓名不能为空等
        if (!StringUtils.hasText(teacher.getName())) {
            throw new IllegalArgumentException("教师姓名不能为空！");
        }
        return teacherRepository.save(teacher);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Teacher> getTeacherByTeacherIdNumber(String teacherIdNumber) {
        return teacherRepository.findByTeacherIdNumber(teacherIdNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Teacher> getTeachers(Pageable pageable) {
        // TODO: 实现带条件的动态查询
        return teacherRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Teacher updateTeacher(Long id, Teacher teacherDetails) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到ID为 " + id + " 的教师"));

        // 校验工号唯一性 (如果工号允许修改且发生改变)
        if (StringUtils.hasText(teacherDetails.getTeacherIdNumber()) &&
                !teacherDetails.getTeacherIdNumber().equals(teacher.getTeacherIdNumber()) &&
                teacherRepository.existsByTeacherIdNumber(teacherDetails.getTeacherIdNumber())) {
            throw new IllegalArgumentException("更新后的教师工号 '" + teacherDetails.getTeacherIdNumber() + "' 已被其他教师使用！");
        }

        // 校验邮箱唯一性 (如果邮箱允许修改且发生改变)
        if (StringUtils.hasText(teacherDetails.getEmail()) &&
                !teacherDetails.getEmail().equals(teacher.getEmail()) && // 只有当email不为空且发生变化时才检查
                teacherRepository.existsByEmail(teacherDetails.getEmail())) {
            Optional<Teacher> existingTeacherWithEmail = teacherRepository.findByEmail(teacherDetails.getEmail());
            if(existingTeacherWithEmail.isPresent() && !existingTeacherWithEmail.get().getId().equals(id)){
                throw new IllegalArgumentException("更新后的电子邮箱 '" + teacherDetails.getEmail() + "' 已被其他教师使用！");
            }
        }
        if (teacherDetails.getName() != null && !StringUtils.hasText(teacherDetails.getName())) {
            throw new IllegalArgumentException("教师姓名不能为空！");
        }


        // 更新允许修改的字段
        if (StringUtils.hasText(teacherDetails.getTeacherIdNumber())) {
            teacher.setTeacherIdNumber(teacherDetails.getTeacherIdNumber());
        }
        if (StringUtils.hasText(teacherDetails.getName())) {
            teacher.setName(teacherDetails.getName());
        }
        if (teacherDetails.getGender() != null) {
            teacher.setGender(teacherDetails.getGender());
        }
        if (teacherDetails.getTitle() != null) {
            teacher.setTitle(teacherDetails.getTitle());
        }
        if (teacherDetails.getDepartment() != null) {
            teacher.setDepartment(teacherDetails.getDepartment());
        }
        // 邮箱允许设置为空
        teacher.setEmail(teacherDetails.getEmail());

        if (teacherDetails.getPhoneNumber() != null) {
            teacher.setPhoneNumber(teacherDetails.getPhoneNumber());
        }
        if (teacherDetails.getTeachableCourses() != null) {
            teacher.setTeachableCourses(teacherDetails.getTeachableCourses());
        }
        if (teacherDetails.getUnavailableSlots() != null) {
            teacher.setUnavailableSlots(teacherDetails.getUnavailableSlots());
        }
        if (teacherDetails.getStatus() != null) {
            teacher.setStatus(teacherDetails.getStatus());
        }
        if (teacherDetails.getRemarks() != null) {
            teacher.setRemarks(teacherDetails.getRemarks());
        }

        return teacherRepository.save(teacher);
    }

    @Override
    @Transactional
    public void deleteTeacher(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new IllegalArgumentException("未找到ID为 " + id + " 的教师，无法删除");
        }
        // TODO: 实际项目中，删除教师前可能需要检查是否有排课任务等关联数据
        teacherRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTeacherIdNumber(String teacherIdNumber, Long excludeId) {
        Optional<Teacher> teacher = teacherRepository.findByTeacherIdNumber(teacherIdNumber);
        return teacher.isPresent() && (excludeId == null || !teacher.get().getId().equals(excludeId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email, Long excludeId) {
        if (!StringUtils.hasText(email)) return false; // 空邮箱不校验
        Optional<Teacher> teacher = teacherRepository.findByEmail(email);
        return teacher.isPresent() && (excludeId == null || !teacher.get().getId().equals(excludeId));
    }
}