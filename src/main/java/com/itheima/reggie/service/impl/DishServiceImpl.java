package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomExctption;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qiao
 * @create 2023-03-24 22:15
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    /**
     * 更新菜品，同时更新菜品对应的口味数据，需要操作两张表：dish,dish_flavor
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品，同时删除菜品以及对应的口味，需要操作两张表：dish,dish_flavor
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDFlavor(List<Long> ids) {
        //查询菜品状态，确认是否可以删除
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);

        int count = this.count(queryWrapper);
        if (count > 0){
            //如果不能删除，抛出一个异常
            throw new CustomExctption("菜品正在售卖中，不能删除");
        }

        //如果可以删除，删除菜品表中的数据---dish
        this.removeByIds(ids);

        //delete from dish_flavor where dish_id in (1,2,3)
        LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        flavorLambdaQueryWrapper.in(DishFlavor::getDishId,ids);

        //删除口味表中的数据---dish_flavor
        dishFlavorService.remove(flavorLambdaQueryWrapper);
    }

    /**
     * 停售或起售菜品，停售时需查看是否有包含菜品的套餐在起售
     * @param ids
     */
    @Override
    public void startAndStopDishWithSetmeal(int status,List<Long> ids) {

        //判断是否是停售
        if (status == 0) {

            //查询菜品关联套餐为起售状态的数量
            int count = setmealService.stopSetmealWithDish(ids);

            if (count > 0) {
                //如果不可以停售，抛出一个异常
                throw new CustomExctption("此菜品在套餐中售卖，不可停售");
            }
        }

        //根据iD进行修改status
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId,ids);
        updateWrapper.set(Dish::getStatus,status);

        //UPDATE dish SET status=? WHERE (id IN (?,?))
        this.update(updateWrapper);
    }

    /**
     * 查询套餐中菜品停售数量
     * @param ids
     * @return
     */
    @Override
    public int startDishWithSetmeal(List<Long> ids) {

        //根据套餐id查询相关联的菜品，添加查询条件
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId,ids);
        queryWrapper.select(SetmealDish::getDishId);

        //查询套餐关联的菜品
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        List<Long> dishId = list.stream().map(SetmealDish::getDishId).collect(Collectors.toList());

        //添加菜品查询条件，status = 1。
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,dishId);
        dishLambdaQueryWrapper.eq(Dish::getStatus,0);

        //查询菜品停售数量
        int count = this.count(dishLambdaQueryWrapper);

        return count;
    }
}
