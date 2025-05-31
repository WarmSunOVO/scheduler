package com.example.intelligent_course_scheduler.payload;

import com.example.intelligent_course_scheduler.entity.Semester;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterDto {
    private Long id;

    @NotBlank(message = "学年不能为空")
    @Size(max = 50)
    private String academicYear;

    @NotBlank(message = "学期名称不能为空")
    @Size(max = 50)
    private String name;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SemesterDto fromEntity(Semester semester) {
        if (semester == null) return null;
        return SemesterDto.builder()
                .id(semester.getId())
                .academicYear(semester.getAcademicYear())
                .name(semester.getName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .isCurrent(semester.getIsCurrent())
                .remarks(semester.getRemarks())
                .createdAt(semester.getCreatedAt())
                .updatedAt(semester.getUpdatedAt())
                .build();
    }

    public Semester toEntity() {
        Semester semester = Semester.builder()
                .academicYear(this.academicYear)
                .name(this.name)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .isCurrent(this.isCurrent != null ? this.isCurrent : false)
                .remarks(this.remarks)
                .build();
        if (this.id != null) { // 如果是更新，保留ID
            semester.setId(this.id);
        }
        return semester;
    }
}