package com.example.seckill.mapper;

import com.example.seckill.entity.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper。
 *
 * @author example
 */
@Mapper
public interface SeckillUserMapper {

    /**
     * 根据ID查询用户
     */
    SeckillUser selectById(@Param("id") Long id);

    /**
     * 根据手机号查询用户
     */
    SeckillUser selectByMobile(@Param("mobile") String mobile);
}
