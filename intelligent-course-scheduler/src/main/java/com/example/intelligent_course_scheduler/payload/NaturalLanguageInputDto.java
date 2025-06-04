package com.example.intelligent_course_scheduler.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NaturalLanguageInputDto {
    @NotBlank(message = "输入内容不能为空")
    private String inputText;
    // 可以添加其他上下文信息，如针对哪个教师、课程等，如果需要的话
    // private Long targetTeacherId;
    // private Long targetCourseId;
}