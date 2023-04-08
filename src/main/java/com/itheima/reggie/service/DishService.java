package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

/**
 * @author qiao
 * @create 2023-03-24 22:11
 */
public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish,dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品，同时更新菜品对应的口味数据，需要操作两张表：dish,dish_flavor
    public void updateWithFlavor(DishDto dishDto);

    //删除菜品，同时删除菜品以及对应的口味，需要操作两张表：dish,dish_flavor
    public void removeWithDFlavor(List<Long> ids);

    //停售或起售菜品，停售时需查看是否有套餐包含菜品
    public void startAndStopDishWithSetmeal(int status,List<Long> ids);

    //查询套餐中菜品停售数量
    public int startDishWithSetmeal(List<Long> ids);
}
