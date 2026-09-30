package com.example.demo.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(
            min = 3,
            max = 50,
            message = "用户名长度必须在 3 到 50 个字符之间"
    )
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(
            min = 5,
            max = 100,
            message = "密码长度必须在 5 到 100 个字符之间"
    )
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名不能超过 50 个字符")
    private String realName;

    @NotBlank(message = "用户角色不能为空")
    @Pattern(
            regexp = "STUDENT|TEACHER|ADMIN",
            message = "用户角色只能是 STUDENT、TEACHER 或 ADMIN"
    )
    private String roleCode;

    @NotBlank(message = "账号状态不能为空")
    @Pattern(
            regexp = "ACTIVE|DISABLED|LOCKED",
            message = "账号状态只能是 ACTIVE、DISABLED 或 LOCKED"
    )
    private String status;
}