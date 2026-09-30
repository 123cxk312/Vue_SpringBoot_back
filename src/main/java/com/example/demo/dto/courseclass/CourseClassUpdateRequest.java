package com.example.demo.dto.courseclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseClassUpdateRequest extends CourseClassRequest {

    @NotBlank(message = "教学班 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "教学班 ID 必须是数字")
    private String id;
}