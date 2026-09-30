package com.example.demo.dto.grade;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreItemRequest {

    @NotBlank(message = "学生 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "学生 ID 必须是数字")
    private String studentId;

    @DecimalMin(value = "0.00", message = "平时成绩不能小于 0")
    @DecimalMax(value = "100.00", message = "平时成绩不能大于 100")
    @Digits(integer = 3, fraction = 2, message = "平时成绩最多保留 2 位小数")
    private BigDecimal usualScore;

    @DecimalMin(value = "0.00", message = "考试成绩不能小于 0")
    @DecimalMax(value = "100.00", message = "考试成绩不能大于 100")
    @Digits(integer = 3, fraction = 2, message = "考试成绩最多保留 2 位小数")
    private BigDecimal examScore;
}