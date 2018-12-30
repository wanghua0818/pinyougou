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
        msgMap.put("mobile","18813961853");
        msgMap.put("signName", "黑马");
        msgMap.put("templateCode", "SMS_125018593");
        msgMap.put("templateParam", "{\"code\":\"123456\"}");
        jmsMessagingTemplate.convertAndSend("itcast_sms_queue", msgMap);
        return "发送消息完成";
    }
}
