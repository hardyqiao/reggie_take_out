package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomExctption;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author qiao
 * @create 2023-03-24 22:13
 */
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据id查询套餐和菜品的关联信息
     * @param id
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //根据id查询套餐信息
        Setmeal setmeal = this.getById(id);

        //拷贝Setmeal到SetmealDto
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //根据套餐id查询菜品关联表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    /**
     * 更新套餐，同时需要更新套餐和菜品的关联数据
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新setmeal表的基本信息
        this.updateById(setmealDto);

        //清理当前套餐对应的菜品数据---setmeal_dish表的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        //添加当前提交过来的菜品关联数据---setmeal_dish表的insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if (count > 0){
            //如果不能删除,抛出一个业务异常
            throw new CustomExctption("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in(1,2,3)
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);

        //删除关系表中的数据---setmeal_dist
        setmealDishService.remove(dishLambdaQueryWrapper);
    }

    /**
     * 停售或起售套餐，起售时需查看是否有包含的菜品被停售
     * @param status
     * @param ids
     */
    @Override
    public void startAndStopSetmealWithDish(int status, List<Long> ids) {

        //判断是否为起售
        if (status == 1){

            //查询菜品关联套餐为起售状态的数量
            int count = dishService.startDishWithSetmeal(ids);

            if (count > 0) {
                //如果不可以停售，抛出一个异常
                throw new CustomExctption("此套餐有菜品在停售，不可起售");
            }
        }

        //添加根据ids修改status状态语句
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Setmeal::getId,ids);
        updateWrapper.set(Setmeal::getStatus,0);

        //修改status状态
        this.update(updateWrapper);
    }

    /**
     * 查询包含菜品的套餐未停售数量
     * @param ids
     * @return
     */
    @Override
    public int stopSetmealWithDish(List<Long> ids) {

        //根据菜品id查询相关联的套餐
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getDishId,ids);
        queryWrapper.select(SetmealDish::getSetmealId);

        //查询菜品相关联的套餐
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        List<Long> setmealIdList = list.stream().map(SetmealDish::getSetmealId).collect(Collectors.toList());

        //添加判断套餐为起售状态的条件
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,setmealIdList);
        setmealLambdaQueryWrapper.in(Setmeal::getStatus,1);

        //查询起售状态的套餐数量
        int count = this.count(setmealLambdaQueryWrapper);

        return count;
    }
}
