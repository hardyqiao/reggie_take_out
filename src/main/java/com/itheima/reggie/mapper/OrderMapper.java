package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author qiao
 * @create 2023-04-07 16:29
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
