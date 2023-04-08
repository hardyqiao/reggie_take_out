package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.event.TransactionalEventListener;
import sun.applet.Main;

/**
 * @author qiao
 * @create 2023-03-10 11:23
 */
@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
public class ReggieAoolication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieAoolication.class,args);
        log.info("项目启动成功...");
    }
}
