package com.example.seckill.service;

import com.example.seckill.dto.LoginDTO;
import com.example.seckill.entity.SeckillUser;
import com.example.seckill.mapper.SeckillUserMapper;
import com.example.seckill.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务：登录校验。
 *
 * @author example
 */
@Service
public class UserService {

    @Autowired
    private SeckillUserMapper userMapper;

    /**
     * 登录校验，成功返回用户对象，失败返回 null。
     */
    public SeckillUser login(LoginDTO loginDTO) {
        if (loginDTO == null || StringUtils.isBlank(loginDTO.getMobile())
                || StringUtils.isBlank(loginDTO.getPassword())) {
            return null;
        }
        SeckillUser user = userMapper.selectByMobile(loginDTO.getMobile());
        if (user == null) {
            return null;
        }
        // 口令校验：MD5(salt + 明文密码)
        String expected = MD5Util.md5WithSalt(user.getSalt(), loginDTO.getPassword());
        return expected.equals(user.getPassword()) ? user : null;
    }

    public SeckillUser getById(Long id) {
        return userMapper.selectById(id);
    }
}
