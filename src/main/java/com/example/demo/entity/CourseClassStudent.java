package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_class_student")
public class CourseClassStudent {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long courseClassId;
    private Long studentId;
    private LocalDateTime createdAt;
}