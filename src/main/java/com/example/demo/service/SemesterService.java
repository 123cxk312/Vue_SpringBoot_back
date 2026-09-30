package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ContextUtil;
import com.example.demo.common.PageResult;
import com.example.demo.dto.semester.SemesterRequest;
import com.example.demo.dto.semester.SemesterResponse;
import com.example.demo.dto.semester.SemesterUpdateRequest;
import com.example.demo.entity.CourseClass;
import com.example.demo.entity.Semester;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CourseClassMapper;
import com.example.demo.mapper.SemesterMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SemesterService {

    private final SemesterMapper semesterMapper;
    private final CourseClassMapper courseClassMapper;

    public SemesterService(
            SemesterMapper semesterMapper,
            CourseClassMapper courseClassMapper
    ) {
        this.semesterMapper = semesterMapper;
        this.courseClassMapper = courseClassMapper;
    }

    public PageResult<SemesterResponse> page(long current, long size) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<Semester> page = semesterMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                Wrappers.<Semester>lambdaQuery()
                        .orderByDesc(Semester::getStartDate)
                        .orderByDesc(Semester::getId)
        );

        List<SemesterResponse> records = page.getRecords()
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

    public List<SemesterResponse> list() {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        return semesterMapper.selectList(
                        Wrappers.<Semester>lambdaQuery()
                                .orderByDesc(Semester::getStartDate)
                                .orderByDesc(Semester::getId)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SemesterResponse detail(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);
        return toResponse(getSemester(id));
    }

    @Transactional
    public void create(SemesterRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);
        validateDates(request);

        String semesterName = normalizeName(request.getSemesterName());

        if (semesterNameExists(semesterName, null)) {
            throw new BusinessException(409, "学期名称已经存在");
        }

        Semester semester = new Semester();
        semester.setSemesterName(semesterName);
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setStatus(request.getStatus());

        semesterMapper.insert(semester);
    }

    @Transactional
    public void update(SemesterUpdateRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);
        validateDates(request);

        Long id = parseId(request.getId());
        Semester semester = getSemester(id);
        String semesterName = normalizeName(request.getSemesterName());

        if (semesterNameExists(semesterName, id)) {
            throw new BusinessException(409, "学期名称已经存在");
        }

        semester.setSemesterName(semesterName);
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setStatus(request.getStatus());

        semesterMapper.updateById(semester);
    }

    @Transactional
    public void delete(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long semesterId = parseId(id);
        Semester semester = getSemester(semesterId);

        if (courseClassMapper.selectCount(
                Wrappers.<CourseClass>lambdaQuery()
                        .eq(CourseClass::getSemesterId, semester.getId())
        ) > 0) {
            throw new BusinessException(
                    409,
                    "请先删除该学期关联的教学班"
            );
        }

        semesterMapper.deleteById(semester.getId());
    }

    private void validateDates(SemesterRequest request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("结束日期必须晚于开始日期");
        }
    }

    private Semester getSemester(String id) {
        return getSemester(parseId(id));
    }

    private Semester getSemester(Long id) {
        Semester semester = semesterMapper.selectById(id);

        if (semester == null) {
            throw new BusinessException(404, "学期不存在");
        }

        return semester;
    }

    private boolean semesterNameExists(
            String semesterName,
            Long excludeId
    ) {
        LambdaQueryWrapper<Semester> wrapper =
                Wrappers.<Semester>lambdaQuery()
                        .eq(Semester::getSemesterName, semesterName);

        if (excludeId != null) {
            wrapper.ne(Semester::getId, excludeId);
        }

        return semesterMapper.selectCount(wrapper) > 0;
    }

    private String normalizeName(String semesterName) {
        return semesterName.trim();
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException("学期 ID 格式错误");
        }
    }

    private SemesterResponse toResponse(Semester semester) {
        return new SemesterResponse(
                String.valueOf(semester.getId()),
                semester.getSemesterName(),
                semester.getStartDate(),
                semester.getEndDate(),
                semester.getStatus(),
                semester.getCreatedAt()
        );
    }
}
