package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.payload.SemesterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface SemesterService {
    SemesterDto createSemester(SemesterDto semesterDto);
    Optional<SemesterDto> getSemesterById(Long id);
    Page<SemesterDto> getAllSemesters(Pageable pageable, String query); // query 可以用于搜索学年或名称
    List<SemesterDto> getAllSemestersList(); // 不分页，用于下拉选择等
    SemesterDto updateSemester(Long id, SemesterDto semesterDto);
    void deleteSemester(Long id);
    // (可选) 设置当前学期等操作
    // void setCurrentSemester(Long id);
}