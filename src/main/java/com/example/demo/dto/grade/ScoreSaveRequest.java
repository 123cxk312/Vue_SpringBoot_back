package com.example.demo.dto.grade;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ScoreSaveRequest {

    @NotNull(message = "成绩列表不能为空")
    private List<@Valid ScoreItemRequest> scores;
}