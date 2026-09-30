package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher")
public class Teacher {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String teacherNo;
    private String title;
    private LocalDateTime createdAt;
}