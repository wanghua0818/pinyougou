package com.itheima.springboot.controller.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RecMsgFromMQ {
    @JmsListener(destination = "springboot_queue")
    public void recMsg(Map<String, Object> msgMap) {
        System.out.println(msgMap+"==============================");
    }
}
