package com.example.seckill.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * MD5 工具类。
 *
 * @author example
 */
public final class MD5Util {

    private MD5Util() {
    }

    /**
     * 原生 MD5
     */
    public static String md5(String src) {
        if (src == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(src.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 加盐 MD5（与入库口令保持一致：md5(salt + 明文)）
     */
    public static String md5WithSalt(String salt, String password) {
        return md5(salt + password);
    }

    /**
     * 计算动态秒杀地址：md5(userId + goodsId + 服务端盐)
     */
    public static String seckillPath(Long userId, Long goodsId, String secretSalt) {
        return md5(userId + "_" + goodsId + "_" + secretSalt);
    }
}
