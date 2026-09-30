package com.example.demo.dto.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentRequest {

    @NotBlank(message = "关联用户 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "关联用户 ID 必须是数字")
    private String userId;

    @NotBlank(message = "学号不能为空")
    @Size(max = 30, message = "学号不能超过 30 个字符")
    private String studentNo;

    @NotBlank(message = "班级不能为空")
    @Size(max = 100, message = "班级不能超过 100 个字符")
    private String className;

    @NotBlank(message = "专业不能为空")
    @Size(max = 100, message = "专业不能超过 100 个字符")
    private String major;

    @NotNull(message = "入学年级不能为空")
    @Min(value = 2000, message = "入学年级不能早于 2000")
    @Max(value = 2100, message = "入学年级不能晚于 2100")
    private Integer gradeYear;
}