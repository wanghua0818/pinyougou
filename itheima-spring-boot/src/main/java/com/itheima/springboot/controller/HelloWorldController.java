package com.itheima.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {
    @Autowired
    private Environment environment;

    @GetMapping("/test")
    public String test() {
        return "品优购,欢迎你的到来" + environment.getProperty("mhy");
    }
}
