package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomExctption;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author qiao
 * @create 2023-04-07 16:32
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {

        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if (shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomExctption("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null){
            throw new CustomExctption("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();//订单号

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);//订单号
            orderDetail.setNumber(item.getNumber());//份数
            orderDetail.setDishFlavor(item.getDishFlavor());//菜品口味关联信息
            orderDetail.setDishId(item.getDishId());//菜品id
            orderDetail.setSetmealId(item.getSetmealId());//套餐id
            orderDetail.setName(item.getName());//菜品名称
            orderDetail.setImage(item.getImage());//菜品图片
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);//订单号
        orders.setOrderTime(LocalDateTime.now());//创建时间
        orders.setCheckoutTime(LocalDateTime.now());//提交时间
        orders.setStatus(2);//支付方式
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);//用户id
        orders.setNumber(String.valueOf(orderId));//订单号
        orders.setUserName(user.getName());//用户姓名
        orders.setConsignee(addressBook.getConsignee());//收货人
        orders.setPhone(addressBook.getPhone());//电话
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    /**
     * 复制订单信息并重新下单
     * @param orders
     */
    @Override
    @Transactional
    public void copyOrdersSubmit(Orders orders) {

        //查询订单信息
        Orders orders1 = super.getById(orders);

        //订单号
        long orderId = IdWorker.getId();

        if (orders1 != null) {
            //添加订单号、订单创建时间、付款时间、状态
            orders1.setId(orderId);
            orders1.setNumber(String.valueOf(orderId));
            orders1.setOrderTime(LocalDateTime.now());
            orders1.setCheckoutTime(LocalDateTime.now());
            orders1.setStatus(2);

            //添加订单信息
            super.save(orders1);
        }

        //添加订单菜品信息表查询条件
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(orders.getId() != null,OrderDetail::getOrderId,orders.getId());

        //查询订单菜品信息表
        List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);

        if (orderDetails != null){
            List<OrderDetail> orderDetails1 = orderDetails.stream().map((item) -> {
                //ID设置为空
                item.setId(null);
                //修改订单号
                item.setOrderId(orderId);
                return item;
            }).collect(Collectors.toList());

            //添加订单菜品关联信息
            orderDetailService.saveBatch(orderDetails1);
        }
    }
}
