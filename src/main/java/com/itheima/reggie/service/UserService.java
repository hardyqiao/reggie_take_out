package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.User;

/**
 * @author qiao
 * @create 2023-04-03 22:31
 */
public interface UserService extends IService<User> {
    //根据用户ID查询用户信息
    public User getById(Long id);
}
