package com.example.demo.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class GradeSheetDetailResponse {

    private String id;
    private String courseClassId;
    private String status;
    private BigDecimal usualWeight;
    private BigDecimal examWeight;
    private List<ScoreRowResponse> rows;
}