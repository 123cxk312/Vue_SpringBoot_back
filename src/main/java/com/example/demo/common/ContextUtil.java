package com.example.demo.common;

import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.AuthException;

public final class ContextUtil {

    private static final ThreadLocal<UserContext> CONTEXT_HOLDER =
            new ThreadLocal<>();

    private ContextUtil() {
    }

    public static void set(UserContext context) {
        CONTEXT_HOLDER.set(context);
    }

    public static UserContext get() {
        UserContext context = CONTEXT_HOLDER.get();

        if (context == null) {
            throw new AuthException("当前请求未登录");
        }

        return context;
    }

    public static Long getUserId() {
        return get().getUserId();
    }

    public static RoleEnum getRole() {
        return get().getRole();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    public static void requireRole(RoleEnum requiredRole) {
        if (getRole() != requiredRole) {
            throw new AuthException(403, "没有权限访问该资源");
        }
    }
}