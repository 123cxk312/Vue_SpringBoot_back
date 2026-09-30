package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ContextUtil;
import com.example.demo.common.PageResult;
import com.example.demo.dto.teacher.TeacherRequest;
import com.example.demo.dto.teacher.TeacherResponse;
import com.example.demo.dto.teacher.TeacherUpdateRequest;
import com.example.demo.entity.CourseClass;
import com.example.demo.entity.SysUser;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CourseClassMapper;
import com.example.demo.mapper.SysUserMapper;
import com.example.demo.mapper.TeacherMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class TeacherService {

    private final TeacherMapper teacherMapper;
    private final SysUserMapper sysUserMapper;
    private final CourseClassMapper courseClassMapper;

    public TeacherService(
            TeacherMapper teacherMapper,
            SysUserMapper sysUserMapper,
            CourseClassMapper courseClassMapper
    ) {
        this.teacherMapper = teacherMapper;
        this.sysUserMapper = sysUserMapper;
        this.courseClassMapper = courseClassMapper;
    }

    public PageResult<TeacherResponse> page(long current, long size) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<Teacher> page = teacherMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                Wrappers.<Teacher>lambdaQuery()
                        .orderByDesc(Teacher::getCreatedAt)
                        .orderByDesc(Teacher::getId)
        );

        List<TeacherResponse> records = page.getRecords()
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

    public List<TeacherResponse> list() {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        return teacherMapper.selectList(
                        Wrappers.<Teacher>lambdaQuery()
                                .orderByAsc(Teacher::getTeacherNo)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TeacherResponse detail(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);
        return toResponse(getTeacher(id));
    }

    @Transactional
    public void create(TeacherRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long userId = parseId(request.getUserId());
        String teacherNo = normalizeTeacherNo(request.getTeacherNo());

        validateUser(userId, null);

        if (teacherNoExists(teacherNo, null)) {
            throw new BusinessException(409, "教师工号已经存在");
        }

        Teacher teacher = new Teacher();
        teacher.setUserId(userId);
        teacher.setTeacherNo(teacherNo);
        teacher.setTitle(request.getTitle().trim());

        teacherMapper.insert(teacher);
    }

    @Transactional
    public void update(TeacherUpdateRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long id = parseId(request.getId());
        Long userId = parseId(request.getUserId());
        Teacher teacher = getTeacher(id);
        String teacherNo = normalizeTeacherNo(request.getTeacherNo());

        validateUser(userId, id);

        if (teacherNoExists(teacherNo, id)) {
            throw new BusinessException(409, "教师工号已经存在");
        }

        teacher.setUserId(userId);
        teacher.setTeacherNo(teacherNo);
        teacher.setTitle(request.getTitle().trim());

        teacherMapper.updateById(teacher);
    }

    @Transactional
    public void delete(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Teacher teacher = getTeacher(id);

        if (courseClassMapper.selectCount(
                Wrappers.<CourseClass>lambdaQuery()
                        .eq(CourseClass::getTeacherId, teacher.getId())
        ) > 0) {
            throw new BusinessException(
                    409,
                    "请先处理该教师关联的教学班"
            );
        }

        teacherMapper.deleteById(teacher.getId());
    }

    private void validateUser(Long userId, Long excludeTeacherId) {
        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(404, "关联用户不存在");
        }

        if (!RoleEnum.TEACHER.getCode().equals(user.getRoleCode())) {
            throw new BusinessException("关联用户的角色必须是 TEACHER");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException("关联用户账号不可用");
        }

        LambdaQueryWrapper<Teacher> wrapper =
                Wrappers.<Teacher>lambdaQuery()
                        .eq(Teacher::getUserId, userId);

        if (excludeTeacherId != null) {
            wrapper.ne(Teacher::getId, excludeTeacherId);
        }

        if (teacherMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "该用户已经关联教师资料");
        }
    }

    private boolean teacherNoExists(
            String teacherNo,
            Long excludeId
    ) {
        LambdaQueryWrapper<Teacher> wrapper =
                Wrappers.<Teacher>lambdaQuery()
                        .eq(Teacher::getTeacherNo, teacherNo);

        if (excludeId != null) {
            wrapper.ne(Teacher::getId, excludeId);
        }

        return teacherMapper.selectCount(wrapper) > 0;
    }

    private Teacher getTeacher(String id) {
        return getTeacher(parseId(id));
    }

    private Teacher getTeacher(Long id) {
        Teacher teacher = teacherMapper.selectById(id);

        if (teacher == null) {
            throw new BusinessException(404, "教师不存在");
        }

        return teacher;
    }

    private String normalizeTeacherNo(String teacherNo) {
        return teacherNo.trim().toUpperCase(Locale.ROOT);
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException("ID 格式错误");
        }
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                String.valueOf(teacher.getId()),
                String.valueOf(teacher.getUserId()),
                teacher.getTeacherNo(),
                teacher.getTitle(),
                teacher.getCreatedAt()
        );
    }
}
