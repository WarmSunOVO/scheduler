package com.example.intelligent_course_scheduler.controller;

import com.example.intelligent_course_scheduler.payload.ConstraintRuleRequestDto;
import com.example.intelligent_course_scheduler.payload.ConstraintRuleResponseDto;
import com.example.intelligent_course_scheduler.service.ConstraintRuleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/constraint-rules")
public class ConstraintRuleController {

    private final ConstraintRuleService constraintRuleService;

    @Autowired
    public ConstraintRuleController(ConstraintRuleService constraintRuleService) {
        this.constraintRuleService = constraintRuleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConstraintRuleResponseDto> create(@Valid @RequestBody ConstraintRuleRequestDto requestDto) {
        return new ResponseEntity<>(constraintRuleService.createConstraintRule(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ConstraintRuleResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(constraintRuleService.getConstraintRuleById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<ConstraintRuleResponseDto>> getAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            @RequestParam(required = false) Map<String, String> searchParams
    ) {
        return ResponseEntity.ok(constraintRuleService.getAllConstraintRules(pageable, searchParams));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConstraintRuleResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody ConstraintRuleRequestDto requestDto) {
        return ResponseEntity.ok(constraintRuleService.updateConstraintRule(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        constraintRuleService.deleteConstraintRule(id);
        return ResponseEntity.ok("Constraint rule with id " + id + " deleted successfully.");
    }

    @GetMapping("/active-for-scheduling/semester/{semesterId}")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('ADMIN')")
    public ResponseEntity<List<ConstraintRuleResponseDto>> getActiveRulesForScheduling(@PathVariable Long semesterId) {
        return ResponseEntity.ok(constraintRuleService.getActiveRulesForScheduling(semesterId));
    }
}