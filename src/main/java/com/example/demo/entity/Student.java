package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String studentNo;
    private String className;
    private String major;
    private Integer gradeYear;
    private LocalDateTime createdAt;
}