package com.example.intelligent_course_scheduler.repository;

import com.example.intelligent_course_scheduler.entity.ClassGroupUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassGroupUnavailabilityRepository extends JpaRepository<ClassGroupUnavailability, Long> {
    List<ClassGroupUnavailability> findByTargetGroupAndSemesterIdAndIsActiveTrue(String targetGroup, Long semesterId);
    List<ClassGroupUnavailability> findBySemesterIdAndIsActiveTrue(Long semesterId);
}
