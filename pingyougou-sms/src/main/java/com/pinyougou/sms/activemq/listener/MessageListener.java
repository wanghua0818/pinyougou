package com.pinyougou.sms.activemq.listener;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.pinyougou.sms.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageListener {
    @Autowired
    private SmsUtil smsUtil;

    /**
     * String mobile,String signName,String templateCode,String templateParam
     * 接收mq队列消息 ,然后利用 阿里大于发短信
     *
     * @param map
     */
    @JmsListener(destination = "itcast_sms_queue")
    public void receiveMsg(Map<String, String> map) {
        try {
            SendSmsResponse response = smsUtil.sendSms(map.get("mobile"), map.get("signName"),
                    map.get("templateCode"), map.get("templateParam"));
            System.out.println("code=" + response.getCode());
            System.out.println("message=" + response.getMessage());
            System.out.println("requestId=" + response.getRequestId());
            System.out.println("bizId=" + response.getBizId());
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
