package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.GradeOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GradeOperationLogMapper
        extends BaseMapper<GradeOperationLog> {
}