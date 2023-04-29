package com.itheima.reggie.controller;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qiao
 * @create 2023-04-07 16:27
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderContorller {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);

        orderService.submit(orders);

        return R.success("下单成功");
    }

    /**
     * 订单分页查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime){
        log.info("page_" + page + "::pageSize_" + pageSize + "::number_" + number + "::beginTime_" + beginTime + "::endTime_" + endTime);
//        LocalDateTime dateTime = LocalDateTime.parse(beginTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        //创建分页对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        //添加查询条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(number != null,Orders::getNumber,number);
        if (beginTime != null && endTime != null) {
            queryWrapper.between(Orders::getOrderTime, beginTime, endTime);
        }
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //查询ordes表的订单信息
        orderService.page(pageInfo,queryWrapper);

        //拷贝除了records的数据
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        //获取records中的数据
        List<Orders> records = pageInfo.getRecords();

        //遍历records中的数据，并复制到ordersDto对象中，通过查询用户信息，为ordersDto对象注入userName属性，最后合并成list集合
        List<OrdersDto> ordersDtos = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //复制数据
            BeanUtils.copyProperties(item,ordersDto);
            //获取用户ID
            Long userId = (Long) item.getUserId();
            //查询用户信息
            User user = userService.getById(userId);
            if (user != null) {
                //添加用户Name
                ordersDto.setUserName(user.getName());
            }
            return ordersDto;
        }).collect(Collectors.toList());

        //注入records值
        ordersDtoPage.setRecords(ordersDtos);

        return R.success(ordersDtoPage);
    }

    /**
     * 订单配送以及完成
     * @param orders
     * @return
     */
    @PutMapping
    public R getStatus(@RequestBody Orders orders){
        log.info("orders=" + orders.toString());

        //根据订单id修改订单信息
        orderService.updateById(orders);

        return R.success("派送成功");
    }

    /**
     * 用户历史订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize){
        log.info("page_" + page + "::pageSize_" + pageSize);

        //创建分页查询对象
        Page<Orders> pageInfo =new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        //获取用户id
        Long userId = BaseContext.getCurrentId();

        //添加查询条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId != null,Orders::getUserId,userId);
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //查询订单信息并进行分页
        orderService.page(pageInfo,queryWrapper);

        //拷贝除了records的数据
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        //获取records中的数据
        List<Orders> records = pageInfo.getRecords();

        //遍历records中的数据，并复制到ordersDto对象中，通过查询用户信息，为ordersDto对象注入orderDetail集合属性，最后合并成list集合
        List<OrdersDto> ordersDtoList = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //复制数据
            BeanUtils.copyProperties(item,ordersDto);
            //获取订单ID
            Long orderId = item.getId();
            //添加订单详细信息查询条件
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(orderId != null,OrderDetail::getOrderId,orderId);
            //获取订单菜品的详细信息
            List<OrderDetail> orderDetails = orderDetailService.list(orderDetailLambdaQueryWrapper);
            if (orderDetails != null){
                //添加详细菜品信息
                ordersDto.setOrderDetails(orderDetails);
            }
            return ordersDto;
        }).collect(Collectors.toList());

        //添加records数据
        ordersDtoPage.setRecords(ordersDtoList);

        return R.success(ordersDtoPage);
    }

    /**
     * 再来一单重新下单
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R copyOrdersSubmit(@RequestBody Orders orders){
        log.info("订单信息：{}",orders);

        //复制订单信息并重新下单
        orderService.copyOrdersSubmit(orders);

        return R.success("下单成功");
    }
}
