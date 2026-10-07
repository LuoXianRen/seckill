package com.example.seckill.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举。
 *
 * @author example
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),

    SESSION_ERROR(401, "用户未登录或登录已失效"),
    ACCESS_LIMIT_REACHED(429, "操作过于频繁，请稍后再试"),

    NOT_FOUND(404, "资源不存在"),
    ERROR(500, "服务异常"),

    PASSWORD_EMPTY(500100, "密码不能为空"),
    MOBILE_EMPTY(500101, "账号不能为空"),
    MOBILE_ERROR(500102, "账号格式错误"),

    SECKILL_NOT_START(500200, "秒杀尚未开始"),
    SECKILL_END(500201, "秒杀已经结束"),
    SECKILL_REPEATE(500202, "该商品已抢购，每人限购一件"),
    SECKILL_SOLD_OUT(500203, "商品已售罄"),
    SECKILL_PATH_INVALID(500204, "秒杀地址无效或已过期"),
    SECKILL_CAPTCHA_ERROR(500205, "验证码错误或已过期"),
    SECKILL_SYSTEM_BUSY(500206, "系统繁忙，请稍后重试"),
    SECKILL_GOODS_NOT_EXIST(500207, "秒杀商品不存在"),
    SECKILL_QUEUEING(500208, "排队中，请稍候查询结果"),
    SECKILL_FAILED(500209, "秒杀失败");

    private final int code;

    private final String message;
}
