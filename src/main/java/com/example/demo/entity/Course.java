package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String courseCode;
    private String courseName;
    private BigDecimal credit;
    private String status;
    private LocalDateTime createdAt;
}