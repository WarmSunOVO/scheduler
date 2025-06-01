package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.payload.RoomUnavailabilityRequestDto;
import com.example.intelligent_course_scheduler.payload.RoomUnavailabilityResponseDto;
import java.util.List;

public interface RoomUnavailabilityService {

    RoomUnavailabilityResponseDto createRoomUnavailability(RoomUnavailabilityRequestDto requestDto);
    List<RoomUnavailabilityResponseDto> getRoomUnavailabilitiesByRoomAndSemester(Long roomId, Long semesterId);
    List<RoomUnavailabilityResponseDto> getRoomUnavailabilitiesBySemester(Long semesterId);
    // (可选) 获取指定教室所有学期的不可用时间
    List<RoomUnavailabilityResponseDto> getRoomUnavailabilitiesByRoom(Long roomId);
    RoomUnavailabilityResponseDto updateRoomUnavailability(Long id, RoomUnavailabilityRequestDto requestDto);
    void deleteRoomUnavailability(Long id);
}