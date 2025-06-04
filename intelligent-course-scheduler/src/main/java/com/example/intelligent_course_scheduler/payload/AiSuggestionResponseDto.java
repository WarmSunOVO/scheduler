package com.example.intelligent_course_scheduler.payload;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSuggestionResponseDto {
    private String originalText;
    private String interpretation; // AI 对输入的理解
    private String suggestionType; // 例如: "UNAVAILABILITY_SUGGESTION", "PREFERENCE_SUGGESTION", "CONSTRAINT_RULE_DRAFT"
    private Map<String, Object> structuredSuggestion; // AI 尝试解析出的结构化数据
    private String humanReadableSuggestion; // 给用户看的、更易懂的建议或操作指引
}