package com.example.demo.dto.courseclassstudent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CourseClassStudentRequest {

    @NotBlank(message = "教学班 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "教学班 ID 必须是数字")
    private String courseClassId;

    @NotBlank(message = "学生 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "学生 ID 必须是数字")
    private String studentId;
}