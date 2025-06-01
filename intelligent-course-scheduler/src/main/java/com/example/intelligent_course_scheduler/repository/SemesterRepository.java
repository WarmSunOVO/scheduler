package com.example.intelligent_course_scheduler.repository; // 确认包名与您项目一致

import com.example.intelligent_course_scheduler.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List; // 引入 List，如果需要返回多个当前学期（理论上不应该）
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long>, JpaSpecificationExecutor<Semester> {

    // --- 您原有的方法 ---
    /**
     * 检查是否存在具有相同学年和名称的学期
     * @param academicYear 学年
     * @param name 学期名称
     * @return 如果存在则返回 true，否则 false
     */
    boolean existsByAcademicYearAndName(String academicYear, String name);

    /**
     * 根据学年和学期名称查找学期
     * @param academicYear 学年
     * @param name 学期名称
     * @return 包含找到的学期的 Optional，如果未找到则为空
     */
    Optional<Semester> findByAcademicYearAndName(String academicYear, String name);

    // --- 新增的方法 ---
    /**
     * 查找当前激活的学期 (isCurrent = true)
     * 理论上最多只有一个当前学期，所以使用 Optional
     * @return 包含当前学期的 Optional，如果不存在则为空
     */
    Optional<Semester> findByIsCurrentTrue();

    /**
     * （可选，如果业务逻辑可能允许多个 current semester，虽然不推荐）
     * 查找所有标记为当前激活的学期
     * @return 当前学期列表
     */
    // List<Semester> findAllByIsCurrentTrue();

}