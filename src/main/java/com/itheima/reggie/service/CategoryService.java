package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

/**
 * @author qiao
 * @create 2023-03-24 16:52
 */
public interface CategoryService extends IService<Category> {

    public void remove(Long id);

}
