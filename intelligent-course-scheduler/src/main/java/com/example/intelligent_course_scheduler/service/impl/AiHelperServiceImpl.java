package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.payload.AiSuggestionResponseDto;
import com.example.intelligent_course_scheduler.payload.NaturalLanguageInputDto;
import com.example.intelligent_course_scheduler.service.AiHelperService;
import com.fasterxml.jackson.core.type.TypeReference; // 用于更安全地转换Map
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List; // 确保导入 List

@Service
public class AiHelperServiceImpl implements AiHelperService {

    private static final Logger logger = LoggerFactory.getLogger(AiHelperServiceImpl.class);
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AiHelperServiceImpl(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AiSuggestionResponseDto processNaturalLanguageInput(NaturalLanguageInputDto inputDto) {
        String userInput = inputDto.getInputText();
        logger.info("接收到自然语言输入: {}", userInput);

        String systemMessage = """
        你是一个用于高校智能排课系统的AI助手。
        你的任务是解析用户输入的关于排课偏好或约束的自然语言描述。
        请尝试从用户输入中识别出以下关键信息（如果存在）：
        - 涉及的人物（如教师姓名）或对象（如课程名称、班级名称、教室编号）。
        - 具体的时间描述（如星期几、上午/下午/晚上、具体节次范围 "第X-Y节"）。
        - 偏好的类型（例如：不希望排课、希望连堂、必须安排在某时间、教室类型要求等）。
        - 如果是偏好，偏好的强度（例如：非常不希望、最好、必须）。

        请将你的分析结果以清晰的JSON对象形式返回，包含以下键（如果适用）：
        "interpretation": (字符串，你对用户输入的理解摘要)
        "structuredSuggestion": (一个JSON对象，包含提取出的结构化信息，例如 {"teacherName": "xxx", "dayOfWeek": 3, "periodOfDay": "AFTERNOON", "preference": "UNWANTED", "strength": "STRONG"} 或 {"courseName": "yyy", "roomType": "多媒体"} )
        "humanReadableAction": (字符串，建议用户接下来可以做什么，例如 "建议为李老师设置周三下午为不可用时间。" 或 "建议为课程数学分析设置教室类型要求为多媒体教室。")
        
        请严格按照上述JSON结构返回，确保 "structuredSuggestion" 是一个嵌套的JSON对象。确保整个回复是一个单一的、合法的JSON对象。
        """;

        String prompt = systemMessage + "\n\n用户输入：\n" + userInput + "\n\nAI助手的JSON格式回复:";
        logger.debug("发送给LLM的Prompt (用户输入部分): {}", userInput);

        String rawAiResponseContent = null;

        try {
            rawAiResponseContent = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            logger.info("LLM 返回的原始字符串: {}", rawAiResponseContent);
        } catch (Exception e) {
            logger.error("调用LLM时发生错误: {}", e.getMessage(), e);
            return new AiSuggestionResponseDto(userInput, "调用AI服务时出错，请检查网络或API配置。", "ERROR", null, "AI服务暂时不可用。");
        }

        String interpretation = "AI未能完全解析您的输入或未返回有效内容。";
        Map<String, Object> structuredSuggestion = new HashMap<>();
        String humanReadableSuggestion = "请根据AI的理解（或原始回复），在系统中手动设置相关约束或偏好。";
        String suggestionType = "GENERAL_SUGGESTION";
        String jsonToParse = rawAiResponseContent; // 默认使用原始回复

        if (rawAiResponseContent != null && !rawAiResponseContent.trim().isEmpty()) {
            String trimmedResponse = rawAiResponseContent.trim();
            // 尝试移除 Markdown 代码块标记
            if (trimmedResponse.startsWith("```json") && trimmedResponse.endsWith("```")) {
                jsonToParse = trimmedResponse.substring(7, trimmedResponse.length() - 3).trim();
                logger.debug("已移除 ```json 标记, 清理后待解析 JSON: {}", jsonToParse);
            } else if (trimmedResponse.startsWith("```") && trimmedResponse.endsWith("```")) {
                jsonToParse = trimmedResponse.substring(3, trimmedResponse.length() - 3).trim();
                logger.debug("已移除 ``` 标记, 清理后待解析 JSON: {}", jsonToParse);
            } else {
                // 如果不是标准的Markdown代码块，但可能包含JSON，尝试查找第一个 '{' 和最后一个 '}'
                int firstBrace = trimmedResponse.indexOf('{');
                int lastBrace = trimmedResponse.lastIndexOf('}');
                if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                    jsonToParse = trimmedResponse.substring(firstBrace, lastBrace + 1);
                    logger.debug("通过定位 {} 提取出的 JSON 字符串: {}", jsonToParse);
                } else {
                    logger.warn("LLM原始回复中未找到清晰的JSON结构 (没有被```包裹，也没有清晰的{}包裹), 将尝试直接解析原始回复。原始回复: {}", rawAiResponseContent);
                }
            }

            try {
                Map<String, Object> aiResponseMap = objectMapper.readValue(jsonToParse, new TypeReference<HashMap<String, Object>>() {});

                Object interpretationObj = aiResponseMap.get("interpretation");
                if (interpretationObj instanceof String) {
                    interpretation = (String) interpretationObj;
                } else if (aiResponseMap.containsKey("interpretation")) {
                    logger.warn("LLM返回的 'interpretation' 字段不是预期的字符串类型: {}", interpretationObj);
                }

                Object structuredSuggestionObj = aiResponseMap.get("structuredSuggestion");
                if (structuredSuggestionObj instanceof Map) {
                    try {
                        // 需要确保 Map 的键是 String，值是 Object
                        structuredSuggestion = objectMapper.convertValue(structuredSuggestionObj, new TypeReference<Map<String, Object>>() {});
                        if (structuredSuggestion.containsKey("teacherName") && structuredSuggestion.containsKey("timeConstraint")) {
                            suggestionType = "TEACHER_UNAVAILABILITY_SUGGESTION";
                        } else if (structuredSuggestion.containsKey("courseName") && structuredSuggestion.containsKey("roomRequirement")) {
                            suggestionType = "COURSE_ROOM_REQUIREMENT_SUGGESTION";
                        }
                    } catch (IllegalArgumentException iae) {
                        logger.warn("LLM返回的 'structuredSuggestion' 字段无法安全转换为 Map<String, Object>: {}", structuredSuggestionObj, iae);
                        structuredSuggestion.put("rawStructuredData", String.valueOf(structuredSuggestionObj)); // 保留原始数据
                    }
                } else if (aiResponseMap.containsKey("structuredSuggestion")) {
                    logger.warn("LLM返回的 'structuredSuggestion' 字段不是预期的Map类型: {}", structuredSuggestionObj);
                }

                Object humanReadableActionObj = aiResponseMap.get("humanReadableAction");
                if (humanReadableActionObj instanceof String) {
                    humanReadableSuggestion = (String) humanReadableActionObj;
                } else if (aiResponseMap.containsKey("humanReadableAction")) {
                    logger.warn("LLM返回的 'humanReadableAction' 字段不是预期的字符串类型: {}", humanReadableActionObj);
                } else if (!structuredSuggestion.isEmpty() && !"AI未能完全解析您的输入或未返回有效内容.".equals(interpretation)) {
                    humanReadableSuggestion = "AI已解析出一些结构化信息，请参考并进行相应设置。";
                }

            } catch (Exception e) {
                logger.warn("解析LLM返回的JSON内容失败 (尝试解析的字符串: '{}'): {}. 原始回复全文: {}", jsonToParse, e.getMessage(), rawAiResponseContent);
                interpretation = "AI回复的JSON格式无法完全解析。原始回复请查看详细日志。用户输入：" + userInput;
                structuredSuggestion.put("rawResponse", rawAiResponseContent);
                humanReadableSuggestion = "AI回复格式错误，建议检查Prompt或联系管理员。";
                suggestionType = "PARSE_ERROR";
            }
        } else {
            interpretation = "AI服务未返回任何内容。";
            logger.warn("LLM调用成功但未返回任何内容。用户输入: {}", userInput);
        }

        return new AiSuggestionResponseDto(userInput, interpretation, suggestionType, structuredSuggestion, humanReadableSuggestion);
    }
}