package com.example.demo.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ScoreRowResponse {

    private String studentId;
    private String studentNo;
    private String realName;
    private BigDecimal usualScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
}