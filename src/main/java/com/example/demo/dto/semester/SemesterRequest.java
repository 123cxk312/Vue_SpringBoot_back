package com.example.demo.dto.semester;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterRequest {

    @NotBlank(message = "学期名称不能为空")
    @Size(max = 100, message = "学期名称不能超过 100 个字符")
    private String semesterName;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @NotBlank(message = "学期状态不能为空")
    @Pattern(
            regexp = "PLANNED|ACTIVE|FINISHED",
            message = "学期状态只能是 PLANNED、ACTIVE 或 FINISHED"
    )
    private String status;
}