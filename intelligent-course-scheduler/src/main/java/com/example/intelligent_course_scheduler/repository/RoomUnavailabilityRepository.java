package com.example.intelligent_course_scheduler.repository;

import com.example.intelligent_course_scheduler.entity.RoomUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomUnavailabilityRepository extends JpaRepository<RoomUnavailability, Long> {
    List<RoomUnavailability> findByClassroomIdAndSemesterIdAndIsActiveTrue(Long classroomId, Long semesterId);
    List<RoomUnavailability> findBySemesterIdAndIsActiveTrue(Long semesterId);
    List<RoomUnavailability> findByClassroomIdAndIsActiveTrue(Long classroomId);
}