package com.example.demo.dto.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseUpdateRequest extends CourseRequest {

    @NotBlank(message = "课程 ID 不能为空")
    private String id;
}