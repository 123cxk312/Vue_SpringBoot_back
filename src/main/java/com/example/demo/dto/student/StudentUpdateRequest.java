package com.example.demo.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentUpdateRequest extends StudentRequest {

    @NotBlank(message = "学生 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "学生 ID 必须是数字")
    private String id;
}