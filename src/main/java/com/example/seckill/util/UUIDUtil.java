package com.example.seckill.util;

import java.util.UUID;

/**
 * UUID 工具类。
 *
 * @author example
 */
public final class UUIDUtil {

    private UUIDUtil() {
    }

    /**
     * 生成不带横线的 UUID（用于登录 token）。
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
