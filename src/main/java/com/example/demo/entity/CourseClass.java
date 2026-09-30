package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_class")
public class CourseClass {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long courseId;
    private Long semesterId;
    private Long teacherId;
    private String className;
    private BigDecimal usualWeight;
    private BigDecimal examWeight;
    private String status;
    private LocalDateTime createdAt;
}