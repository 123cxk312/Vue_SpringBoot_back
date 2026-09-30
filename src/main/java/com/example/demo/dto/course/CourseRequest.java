package com.example.demo.dto.course;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseRequest {

    @NotBlank(message = "课程编码不能为空")
    @Size(max = 30, message = "课程编码不能超过 30 个字符")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称不能超过 100 个字符")
    private String courseName;

    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.1", message = "学分必须大于 0")
    @Digits(integer = 3, fraction = 1, message = "学分最多 3 位整数和 1 位小数")
    private BigDecimal credit;

    @NotBlank(message = "课程状态不能为空")
    @Pattern(
            regexp = "ACTIVE|INACTIVE",
            message = "课程状态只能是 ACTIVE 或 INACTIVE"
    )
    private String status;
}