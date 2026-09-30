package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("grade_operation_log")
public class GradeOperationLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long gradeSheetId;
    private Long operatorId;
    private String action;
    private String beforeStatus;
    private String afterStatus;
    private String remark;
    private LocalDateTime createdAt;
}