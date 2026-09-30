package com.example.demo.dto.courseclass;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseClassRequest {

    @NotBlank(message = "课程 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "课程 ID 必须是数字")
    private String courseId;

    @NotBlank(message = "学期 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "学期 ID 必须是数字")
    private String semesterId;

    @NotBlank(message = "教师 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "教师 ID 必须是数字")
    private String teacherId;

    @NotBlank(message = "教学班名称不能为空")
    @Size(max = 100, message = "教学班名称不能超过 100 个字符")
    private String className;

    @NotNull(message = "平时成绩权重不能为空")
    @DecimalMin(value = "0.0", message = "平时成绩权重不能小于 0")
    @DecimalMax(value = "1.0", message = "平时成绩权重不能大于 1")
    @Digits(integer = 1, fraction = 4, message = "平时成绩权重最多保留 4 位小数")
    private BigDecimal usualWeight;

    @NotNull(message = "考试成绩权重不能为空")
    @DecimalMin(value = "0.0", message = "考试成绩权重不能小于 0")
    @DecimalMax(value = "1.0", message = "考试成绩权重不能大于 1")
    @Digits(integer = 1, fraction = 4, message = "考试成绩权重最多保留 4 位小数")
    private BigDecimal examWeight;

    @NotBlank(message = "教学班状态不能为空")
    @Pattern(
            regexp = "ACTIVE|INACTIVE",
            message = "教学班状态只能是 ACTIVE 或 INACTIVE"
    )
    private String status;
}