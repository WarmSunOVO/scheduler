package com.example.intelligent_course_scheduler.payload;

// ... (imports与CreateRequest类似)
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeachingTaskUpdateRequest {
    // 通常 semesterId, courseId, targetGroup 不允许修改，如果需要修改，则相当于删除旧任务，创建新任务
    // @NotNull private Long semesterId;
    // @NotNull private Long courseId;
    // @NotBlank @Size(max = 255) private String targetGroup;

    @NotNull(message = "教师ID不能为空") // 教师可以改派
    private Long teacherId;

    @NotNull(message = "周学时不能为空")
    @Min(value = 1, message = "周学时必须大于0")
    private Integer hoursPerWeek;

    @NotNull(message = "每周上课次数不能为空")
    @Min(value = 1, message = "每周上课次数必须大于0")
    private Integer sessionsPerWeek;

    @NotNull(message = "每次连堂节数不能为空")
    @Min(value = 1, message = "每次连堂节数必须大于0")
    private Integer sessionLength;

    @Size(max = 50)
    private String requiredRoomType;

    private String remarks;
}