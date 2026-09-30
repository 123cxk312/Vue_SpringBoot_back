package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long gradeSheetId;
    private Long studentId;
    private BigDecimal usualScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}