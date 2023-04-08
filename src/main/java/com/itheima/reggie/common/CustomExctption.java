package com.itheima.reggie.common;

/**
 * 自定义业务异常
 * @author qiao
 * @create 2023-03-24 22:40
 */
public class CustomExctption extends RuntimeException{
    public CustomExctption(String message){
        super(message);
    }
}
