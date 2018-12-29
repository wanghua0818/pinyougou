package com.itheima.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/mq")
@RestController
public class MQcontroller {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @GetMapping("/send")
    public String sendMsg() {
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("id", 123);
        msgMap.put("name", "stone");
        jmsMessagingTemplate.convertAndSend("springboot_queue", msgMap);
        return "发送消息完成";
    }
}
