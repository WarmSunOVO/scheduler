package com.example.intelligent_course_scheduler.payload;

import com.example.intelligent_course_scheduler.entity.Course;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;

    @NotBlank(message = "课程代码不能为空")
    @Size(max = 50)
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100)
    private String name;

    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.1", message = "学分必须大于0")
    private Double credits;

    private Integer hoursPerWeek;
    private Integer totalHours;

    @Size(max = 50)
    private String type;

    @Size(max = 50)
    private String requiredRoomType;

    @Size(max = 100)
    private String departmentName;

    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseDto fromEntity(Course course) {
        if (course == null) return null;
        return CourseDto.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .credits(course.getCredits())
                .hoursPerWeek(course.getHoursPerWeek())
                .totalHours(course.getTotalHours())
                .type(course.getType())
                .requiredRoomType(course.getRequiredRoomType())
                .departmentName(course.getDepartmentName())
                .description(course.getDescription())
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    public Course toEntity() {
        Course course = Course.builder()
                .courseCode(this.courseCode)
                .name(this.name)
                .credits(this.credits)
                .hoursPerWeek(this.hoursPerWeek)
                .totalHours(this.totalHours)
                .type(this.type)
                .requiredRoomType(this.requiredRoomType)
                .departmentName(this.departmentName)
                .description(this.description)
                .status(StringUtils.hasText(this.status) ? this.status : "激活") // 确保有默认值
                .build();
        if (this.id != null) {
            course.setId(this.id);
        }
        return course;
    }
    // StringUtils 需要导入 org.springframework.util.StringUtils;
    // 在 toEntity 中用到，但如果 DTO 不负责转换，可以在 Service 中处理默认值
}