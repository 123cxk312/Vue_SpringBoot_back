package com.example.demo.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ContextUtil;
import com.example.demo.common.PageResult;
import com.example.demo.dto.user.UserCreateRequest;
import com.example.demo.dto.user.UserResponse;
import com.example.demo.dto.user.UserUpdateRequest;
import com.example.demo.entity.Student;
import com.example.demo.entity.SysUser;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.mapper.SysUserMapper;
import com.example.demo.mapper.TeacherMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    public UserService(
            SysUserMapper sysUserMapper,
            StudentMapper studentMapper,
            TeacherMapper teacherMapper
    ) {
        this.sysUserMapper = sysUserMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
    }

    public PageResult<UserResponse> page(long current, long size) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<SysUser> page = sysUserMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                Wrappers.<SysUser>lambdaQuery()
                        .orderByDesc(SysUser::getCreatedAt)
                        .orderByDesc(SysUser::getId)
        );

        List<UserResponse> records = page.getRecords()
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

    public List<UserResponse> list() {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        return sysUserMapper.selectList(
                        Wrappers.<SysUser>lambdaQuery()
                                .orderByAsc(SysUser::getUsername)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse detail(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);
        return toResponse(getUser(id));
    }

    @Transactional
    public void create(UserCreateRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        String username = request.getUsername().trim();
        RoleEnum role = requireRole(request.getRoleCode());

        if (usernameExists(username, null)) {
            throw new BusinessException(409, "用户名已经存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(encodePassword(request.getPassword()));
        user.setRealName(request.getRealName().trim());
        user.setRoleCode(role.getCode());
        user.setStatus(request.getStatus());

        sysUserMapper.insert(user);
    }

    @Transactional
    public void update(UserUpdateRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long id = parseId(request.getId());
        SysUser user = getUser(id);
        String username = request.getUsername().trim();
        RoleEnum role = requireRole(request.getRoleCode());

        if (usernameExists(username, id)) {
            throw new BusinessException(409, "用户名已经存在");
        }

        validateSelfAccount(user, role, request.getStatus());
        validateRoleCompatibility(user.getId(), role);

        user.setUsername(username);
        user.setRealName(request.getRealName().trim());
        user.setRoleCode(role.getCode());
        user.setStatus(request.getStatus());

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {
            user.setPassword(encodePassword(request.getPassword()));
        }

        sysUserMapper.updateById(user);
    }

    @Transactional
    public void delete(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        SysUser user = getUser(id);
        Long currentUserId = ContextUtil.getUserId();

        if (user.getId().equals(currentUserId)) {
            throw new BusinessException("不能删除当前登录账号");
        }

        if (studentMapper.selectCount(
                Wrappers.<Student>lambdaQuery()
                        .eq(Student::getUserId, user.getId())
        ) > 0) {
            throw new BusinessException(
                    409,
                    "请先删除该用户关联的学生资料"
            );
        }

        if (teacherMapper.selectCount(
                Wrappers.<Teacher>lambdaQuery()
                        .eq(Teacher::getUserId, user.getId())
        ) > 0) {
            throw new BusinessException(
                    409,
                    "请先删除该用户关联的教师资料"
            );
        }

        try {
            sysUserMapper.deleteById(user.getId());
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    409,
                    "该用户已被业务数据引用，无法删除"
            );
        }
    }

    private void validateSelfAccount(
            SysUser user,
            RoleEnum role,
            String status
    ) {
        if (!user.getId().equals(ContextUtil.getUserId())) {
            return;
        }

        if (role != RoleEnum.ADMIN) {
            throw new BusinessException(
                    "不能修改当前登录管理员的角色"
            );
        }

        if (!"ACTIVE".equals(status)) {
            throw new BusinessException(
                    "不能禁用或锁定当前登录账号"
            );
        }
    }

    private void validateRoleCompatibility(
            Long userId,
            RoleEnum role
    ) {
        boolean hasStudent = studentMapper.selectCount(
                Wrappers.<Student>lambdaQuery()
                        .eq(Student::getUserId, userId)
        ) > 0;

        boolean hasTeacher = teacherMapper.selectCount(
                Wrappers.<Teacher>lambdaQuery()
                        .eq(Teacher::getUserId, userId)
        ) > 0;

        if (hasStudent && role != RoleEnum.STUDENT) {
            throw new BusinessException(
                    409,
                    "该用户已有学生资料，角色必须是 STUDENT"
            );
        }

        if (hasTeacher && role != RoleEnum.TEACHER) {
            throw new BusinessException(
                    409,
                    "该用户已有教师资料，角色必须是 TEACHER"
            );
        }
    }

    private RoleEnum requireRole(String roleCode) {
        RoleEnum role = RoleEnum.fromCode(roleCode);

        if (role == null) {
            throw new BusinessException("用户角色无效");
        }

        return role;
    }

    private String encodePassword(String rawPassword) {
        if (rawPassword == null
                || rawPassword.isBlank()) {
            throw new BusinessException("密码不能为空");
        }

        if (rawPassword.length() < 5) {
            throw new BusinessException(
                    "密码长度不能少于 5 个字符"
            );
        }

        return BCrypt.hashpw(rawPassword);
    }

    private boolean usernameExists(
            String username,
            Long excludeId
    ) {
        LambdaQueryWrapper<SysUser> wrapper =
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, username);

        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }

        return sysUserMapper.selectCount(wrapper) > 0;
    }

    private SysUser getUser(String id) {
        return getUser(parseId(id));
    }

    private SysUser getUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);

        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        return user;
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException("用户 ID 格式错误");
        }
    }

    private UserResponse toResponse(SysUser user) {
        return new UserResponse(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getRealName(),
                user.getRoleCode(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}