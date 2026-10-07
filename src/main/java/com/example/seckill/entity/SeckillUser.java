package com.example.seckill.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀用户表实体。
 *
 * @author example
 */
@Data
public class SeckillUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号（登录账号）
     */
    private String mobile;

    /**
     * 登录密码（MD5(salt + 明文)）
     */
    private String password;

    /**
     * 密码盐
     */
    private String salt;

    /**
     * 头像地址
     */
    private String head;

    /**
     * 注册时间
     */
    private Date registerDate;
}
