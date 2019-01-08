package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
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
    private SeckillOrderService seckillOrderService;
    @Reference
    private PayService payService;

    /**
     * 根据支付日志id 到微信支付创建支付订单并返回支付二维码等信息
     *
     * @return 支付二维码链接地址等信息
     */
    @GetMapping("/createNative")
    public Map<String, String> createNative(String outTradeNo) {
        try {
            TbSeckillOrder seckillOrder = seckillOrderService.findSeckillOrderInRedisByOutTradeNo(outTradeNo);
            if (seckillOrder != null) {
                //本次要支付的总金额
                String totalFee = (long) (seckillOrder.getMoney().doubleValue() * 100) + "";
                //到支付系统提交订单 并返回支付地址
                Map<String, String> map = payService.createNative(outTradeNo, totalFee);
                return map;
            }
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
            //1分钟内查询
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
                    seckillOrderService.saveSeckillOrderInRedisToDb(outTradeNo, map.get("transaction_id"));
                    result = Result.ok("查询支付状态成功");
                    break;
                }
                count++;
                if (count > 20) {
                    result = Result.fail("支付超时");
                    //关闭订单成功；将redis中的订单删除并更新秒杀商品剩余库存
                    seckillOrderService.removeSeckillOrderInRedis(outTradeNo);
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