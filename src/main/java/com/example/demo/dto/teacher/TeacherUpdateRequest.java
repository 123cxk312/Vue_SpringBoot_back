package com.example.demo.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherUpdateRequest extends TeacherRequest {

    @NotBlank(message = "教师 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "教师 ID 必须是数字")
    private String id;
}