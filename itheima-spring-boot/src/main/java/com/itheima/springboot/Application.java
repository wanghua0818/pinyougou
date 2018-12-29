package com.itheima.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * springboot引导类
 * 如果是springboot引导类,需要加上SpringBootApplication的注解
 * 默认将扫描该包及其子包的注解
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
