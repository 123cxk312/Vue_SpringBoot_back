package com.example.demo.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeacherRequest {

    @NotBlank(message = "关联用户 ID 不能为空")
    @Pattern(regexp = "\\d+", message = "关联用户 ID 必须是数字")
    private String userId;

    @NotBlank(message = "教师工号不能为空")
    @Size(max = 30, message = "教师工号不能超过 30 个字符")
    private String teacherNo;

    @NotBlank(message = "职称不能为空")
    @Size(max = 50, message = "职称不能超过 50 个字符")
    private String title;
}