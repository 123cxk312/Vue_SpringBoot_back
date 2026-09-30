package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("grade_sheet")
public class GradeSheet {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long courseClassId;
    private String status;
    private Long submittedBy;
    private LocalDateTime submittedAt;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime publishedAt;
    private String reviewComment;
    private LocalDateTime createdAt;
}