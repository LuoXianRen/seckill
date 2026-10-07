package com.example.seckill.util;

import com.example.seckill.entity.SeckillUser;

/**
 * 当前登录用户上下文（基于 ThreadLocal）。
 *
 * @author example
 */
public final class UserContext {

    private static final ThreadLocal<SeckillUser> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUser(SeckillUser user) {
        USER_HOLDER.set(user);
    }

    public static SeckillUser getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        SeckillUser user = getUser();
        return user == null ? null : user.getId();
    }

    public static void remove() {
        USER_HOLDER.remove();
    }
}
