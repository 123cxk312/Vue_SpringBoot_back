package com.example.demo.dto.grade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotBlank(message = "审核意见不能为空")
    @Size(max = 1000, message = "审核意见不能超过 1000 个字符")
    private String comment;
}