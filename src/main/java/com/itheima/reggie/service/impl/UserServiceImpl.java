package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.UserMapper;
import com.itheima.reggie.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author qiao
 * @create 2023-04-03 22:31
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 根据用户ID查询用户信息
     * @param id
     * @return
     */
    @Override
    public User getById(Long id) {

        //根据id查询用户信息
        User user = super.getById(id);

        return user;
    }
}
