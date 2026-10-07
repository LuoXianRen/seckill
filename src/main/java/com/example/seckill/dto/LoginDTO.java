package com.example.seckill.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求参数。
 *
 * @author example
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 手机号 / 昵称
     */
    @NotBlank(message = "账号不能为空")
    private String mobile;

    /**
     * 明文密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
