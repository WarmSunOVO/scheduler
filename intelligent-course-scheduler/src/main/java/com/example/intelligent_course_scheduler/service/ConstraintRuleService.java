package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.payload.ConstraintRuleRequestDto;
import com.example.intelligent_course_scheduler.payload.ConstraintRuleResponseDto;
import org.springframework.data.domain.Page; // 引入分页
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map; // 用于动态查询参数

public interface ConstraintRuleService {
    ConstraintRuleResponseDto createConstraintRule(ConstraintRuleRequestDto requestDto);
    ConstraintRuleResponseDto getConstraintRuleById(Long id);
    Page<ConstraintRuleResponseDto> getAllConstraintRules(Pageable pageable, Map<String, String> searchParams); // 支持分页和搜索
    ConstraintRuleResponseDto updateConstraintRule(Long id, ConstraintRuleRequestDto requestDto);
    void deleteConstraintRule(Long id);
    List<ConstraintRuleResponseDto> getActiveRulesForScheduling(Long semesterId); // 获取排课引擎需要的所有激活规则
}