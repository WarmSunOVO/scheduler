package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.payload.RoomUnavailabilityRequestDto;
import com.example.intelligent_course_scheduler.payload.RoomUnavailabilityResponseDto;
import com.example.intelligent_course_scheduler.service.RoomUnavailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-unavailabilities")
public class RoomUnavailabilityController {

    private final RoomUnavailabilityService roomUnavailabilityService;

    @Autowired
    public RoomUnavailabilityController(RoomUnavailabilityService roomUnavailabilityService) {
        this.roomUnavailabilityService = roomUnavailabilityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RoomUnavailabilityResponseDto> create(@Valid @RequestBody RoomUnavailabilityRequestDto requestDto) {
        return new ResponseEntity<>(roomUnavailabilityService.createRoomUnavailability(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/room/{roomId}/semester/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<RoomUnavailabilityResponseDto>> getByRoomAndSemester(
            @PathVariable Long roomId, @PathVariable Long semesterId) {
        return ResponseEntity.ok(roomUnavailabilityService.getRoomUnavailabilitiesByRoomAndSemester(roomId, semesterId));
    }

    @GetMapping("/semester/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<RoomUnavailabilityResponseDto>> getBySemester(@PathVariable Long semesterId) {
        return ResponseEntity.ok(roomUnavailabilityService.getRoomUnavailabilitiesBySemester(semesterId));
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<RoomUnavailabilityResponseDto>> getByRoom(@PathVariable Long roomId){
        return ResponseEntity.ok(roomUnavailabilityService.getRoomUnavailabilitiesByRoom(roomId));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RoomUnavailabilityResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody RoomUnavailabilityRequestDto requestDto) {
        return ResponseEntity.ok(roomUnavailabilityService.updateRoomUnavailability(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        roomUnavailabilityService.deleteRoomUnavailability(id);
        return ResponseEntity.ok("Room unavailability with id " + id + " deleted successfully.");
    }
}