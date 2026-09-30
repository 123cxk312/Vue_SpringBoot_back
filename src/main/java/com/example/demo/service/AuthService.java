package com.example.demo.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.dto.auth.UserProfile;
import com.example.demo.entity.Student;
import com.example.demo.entity.SysUser;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.AuthException;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.mapper.SysUserMapper;
import com.example.demo.mapper.TeacherMapper;
import com.example.demo.util.JwtUtil;
import org.springframework.stereotype.Service;
import com.example.demo.common.ContextUtil;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final JwtUtil jwtUtil;

    public AuthService(
            SysUserMapper sysUserMapper,
            StudentMapper studentMapper,
            TeacherMapper teacherMapper,
            JwtUtil jwtUtil
    ) {
        this.sysUserMapper = sysUserMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();

        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, username)
        );

        if (user == null
                || !passwordMatches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new AuthException("用户名或密码错误");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new AuthException(403, "账号已禁用或锁定");
        }

        RoleEnum role = RoleEnum.fromCode(user.getRoleCode());

        if (role == null) {
            throw new AuthException(403, "账号角色无效");
        }

        String token = jwtUtil.generateToken(user.getId(), role);

        UserProfile profile = toUserProfile(user);

        return new LoginResponse(token, profile);
    }

    private boolean passwordMatches(
            String rawPassword,
            String passwordHash
    ) {
        try {
            return BCrypt.checkpw(rawPassword, passwordHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public UserProfile currentUser() {
        Long userId = ContextUtil.getUserId();

        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            throw new AuthException("当前用户不存在");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new AuthException(403, "账号已禁用或锁定");
        }

        return toUserProfile(user);
    }

    private UserProfile toUserProfile(SysUser user) {
        RoleEnum role = RoleEnum.fromCode(user.getRoleCode());

        if (role == null) {
            throw new AuthException(403, "账号角色无效");
        }

        String studentId = null;
        String teacherId = null;

        if (role == RoleEnum.STUDENT) {
            Student student = studentMapper.selectOne(
                    Wrappers.<Student>lambdaQuery()
                            .eq(Student::getUserId, user.getId())
            );

            if (student == null) {
                throw new AuthException(403, "当前账号没有学生资料");
            }

            studentId = String.valueOf(student.getId());
        } else if (role == RoleEnum.TEACHER) {
            Teacher teacher = teacherMapper.selectOne(
                    Wrappers.<Teacher>lambdaQuery()
                            .eq(Teacher::getUserId, user.getId())
            );

            if (teacher == null) {
                throw new AuthException(403, "当前账号没有教师资料");
            }

            teacherId = String.valueOf(teacher.getId());
        }

        return new UserProfile(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getRealName(),
                role.getCode(),
                user.getStatus(),
                studentId,
                teacherId
        );
    }

}
