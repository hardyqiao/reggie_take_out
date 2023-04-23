package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.event.TransactionalEventListener;
import sun.applet.Main;

/**
 * @author qiao
 * @create 2023-03-10 11:23
 */
@Slf4j
@SpringBootApplication
@ServletComponentScan          //Servlet（控制器）、Filter（过滤器）、Listener（监听器）可以直接通过@WebServlet、@WebFilter、@WebListener注解自动注册到Spring容器中，无需其他代码。
@EnableTransactionManagement   //开启事务注解功能
@EnableCaching                 //开启缓存Spring cache注解功能
public class ReggieAoolication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieAoolication.class,args);
        log.info("项目启动成功...");
    }
}
