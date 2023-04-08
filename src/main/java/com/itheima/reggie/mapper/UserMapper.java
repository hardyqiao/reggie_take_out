package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author qiao
 * @create 2023-04-03 22:29
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
