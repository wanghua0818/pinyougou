package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.PayService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference
    private PayService payService;
    @Reference
    private OrderService orderService;
    /**
     * 根据支付日志id 到微信支付创建支付订单并返回支付二维码等信息
     *
     * @return 支付二维码链接地址等信息
     */
    @GetMapping("/createNative")
    public Map<String, String> createNative(String outTradeNo) {
        try {
            TbPayLog payLog = orderService.findPayLogByOutTradeNo(outTradeNo);
            String totalFee = payLog.getTotalFee() + "";
            Map<String, String> map = payService.createNative(outTradeNo, totalFee);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    //  return $http.get("pay/queryPayStatus.do?outTradeNo=" + outTradeNo + "&t=" + Math.random())
    @GetMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo) {
        Result result = Result.fail("支付失败");
        try {
            //3分钟内查询
            int count = 0;
            while (true) {
                //1.编写处理器方法无限循环去查询支付系统中订单的支付状态;
                Map<String, String> map = payService.queryPayStatus(outTradeNo);
                if (map == null) {
                    //如果查询失败则退出循环;
                    break;
                }
                if ("SUCCESS".equals(map.get("trade_state"))) {
                    //3.如果查询到订单已支付,调用业务方法更新订单状态,返回查询成功.
                    orderService.updateOrderStatus(outTradeNo, map.get("transaction_id"));
                    result = Result.ok("查询支付状态成功");
                    break;
                }
                count++;
                if (count > 60) {
                    result = Result.fail("支付超时");
                    break;
                }
                //每隔3秒
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }
}