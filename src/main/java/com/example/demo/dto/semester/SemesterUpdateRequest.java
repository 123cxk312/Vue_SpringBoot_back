package com.example.demo.dto.semester;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SemesterUpdateRequest extends SemesterRequest {

    @NotBlank(message = "学期 ID 不能为空")
    private String id;
}