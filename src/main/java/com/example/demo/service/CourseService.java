package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ContextUtil;
import com.example.demo.common.PageResult;
import com.example.demo.dto.course.CourseRequest;
import com.example.demo.dto.course.CourseResponse;
import com.example.demo.dto.course.CourseUpdateRequest;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseClass;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CourseClassMapper;
import com.example.demo.mapper.CourseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CourseService {

    private final CourseMapper courseMapper;
    private final CourseClassMapper courseClassMapper;

    public CourseService(
            CourseMapper courseMapper,
            CourseClassMapper courseClassMapper
    ) {
        this.courseMapper = courseMapper;
        this.courseClassMapper = courseClassMapper;
    }

    public PageResult<CourseResponse> page(long current, long size) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<Course> page = courseMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                Wrappers.<Course>lambdaQuery()
                        .orderByDesc(Course::getCreatedAt)
                        .orderByDesc(Course::getId)
        );

        List<CourseResponse> records = page.getRecords()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                records
        );
    }

    public List<CourseResponse> list() {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        return courseMapper.selectList(
                        Wrappers.<Course>lambdaQuery()
                                .orderByAsc(Course::getCourseCode)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CourseResponse detail(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);
        return toResponse(getCourse(id));
    }

    @Transactional
    public void create(CourseRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        String courseCode = normalizeCode(request.getCourseCode());

        if (courseCodeExists(courseCode, null)) {
            throw new BusinessException(409, "课程编码已经存在");
        }

        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(request.getCourseName().trim());
        course.setCredit(request.getCredit());
        course.setStatus(request.getStatus());

        courseMapper.insert(course);
    }

    @Transactional
    public void update(CourseUpdateRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long id = parseId(request.getId());
        Course course = getCourse(id);
        String courseCode = normalizeCode(request.getCourseCode());

        if (courseCodeExists(courseCode, id)) {
            throw new BusinessException(409, "课程编码已经存在");
        }

        course.setCourseCode(courseCode);
        course.setCourseName(request.getCourseName().trim());
        course.setCredit(request.getCredit());
        course.setStatus(request.getStatus());

        courseMapper.updateById(course);
    }

    @Transactional
    public void delete(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long courseId = parseId(id);
        Course course = getCourse(courseId);

        if (courseClassMapper.selectCount(
                Wrappers.<CourseClass>lambdaQuery()
                        .eq(CourseClass::getCourseId, course.getId())
        ) > 0) {
            throw new BusinessException(
                    409,
                    "请先删除该课程关联的教学班"
            );
        }

        courseMapper.deleteById(course.getId());
    }

    private Course getCourse(String id) {
        return getCourse(parseId(id));
    }

    private Course getCourse(Long id) {
        Course course = courseMapper.selectById(id);

        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        return course;
    }

    private boolean courseCodeExists(
            String courseCode,
            Long excludeId
    ) {
        LambdaQueryWrapper<Course> wrapper =
                Wrappers.<Course>lambdaQuery()
                        .eq(Course::getCourseCode, courseCode);

        if (excludeId != null) {
            wrapper.ne(Course::getId, excludeId);
        }

        return courseMapper.selectCount(wrapper) > 0;
    }

    private String normalizeCode(String courseCode) {
        return courseCode.trim().toUpperCase(Locale.ROOT);
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException("课程 ID 格式错误");
        }
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                String.valueOf(course.getId()),
                course.getCourseCode(),
                course.getCourseName(),
                course.getCredit(),
                course.getStatus(),
                course.getCreatedAt()
        );
    }
}
