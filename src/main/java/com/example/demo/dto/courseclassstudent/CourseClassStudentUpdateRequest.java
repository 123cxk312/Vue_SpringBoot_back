package com.example.demo.dto.courseclassstudent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseClassStudentUpdateRequest
        extends CourseClassStudentRequest {

    @NotBlank(message = "选课关系 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "选课关系 ID 必须是数字")
    private String id;
}