package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.CourseClassStudent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseClassStudentMapper
        extends BaseMapper<CourseClassStudent> {
}