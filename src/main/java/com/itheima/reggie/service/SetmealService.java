package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @author qiao
 * @create 2023-03-24 22:12
 */
public interface SetmealService extends IService<Setmeal> {

    //新增套餐，同时需要保存套餐和菜品的关联数据
    public void saveWithDish(SetmealDto setmealDto);

    //根据id查询套餐和菜品的关联信息
    public SetmealDto getByIdWithDish(Long id);

    //更新套餐，同时需要更新套餐和菜品的关联数据
    public void updateWithDish(SetmealDto setmealDto);

    //删除套餐，同时需要删除套餐和菜品的关联数据
    public void removeWithDish(List<Long> ids);

    //停售或起售套餐，起售时需查看是否有包含的菜品被停售
    public void startAndStopSetmealWithDish(int status,List<Long> ids);

    //查询包含菜品的套餐未停售数量
    public int stopSetmealWithDish(List<Long> ids);
}
