package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录表用户id
 * @author qiao
 * @create 2023-03-24 16:27
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置值
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
