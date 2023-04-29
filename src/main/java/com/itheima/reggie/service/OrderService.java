package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

/**
 * @author qiao
 * @create 2023-04-07 16:30
 */
public interface OrderService extends IService<Orders> {

    //用户下单
    public void submit(Orders orders);
    //复制订单信息并重新下单
    public void copyOrdersSubmit(Orders orders);
}
