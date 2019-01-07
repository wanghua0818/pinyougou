package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.service.PayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service(interfaceClass = PayService.class)
public class PayServiceImpl implements PayService {
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;

    /**
     * 到支付系统获取支付二维码链接地址等信息
     * （二维码链接地址code_url、交易订单号、本次支付的总金额totalFee、result_code业务结果（SUCCESS表示成功））
     *
     * @param outTradeNo 交易订单号
     * @param totalFee   支付金额
     * @return 操作结果（二维码链接地址code_url、交易订单号、本次支付的总金额totalFee、result_code业务结果（SUCCESS表示成功））
     */
    @Override
    public Map<String, String> createNative(String outTradeNo, String totalFee) {
        Map<String, String> returnMap = new HashMap<>();
        try {
            //1. 封装微信需要的参数；
            Map<String, String> param = new HashMap<>();
            //公众账号ID
            param.put("appid", appid);
            //商户号
            param.put("mch_id", partner);
            //随机字符串；可以利用微信提供的工具类生成
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；可以在发送的时候利用工具类生成
            //param.put("sign", null);
            //商品描述
            param.put("body", "ee95品优购");
            //商户订单号
            param.put("out_trade_no", outTradeNo);
            //标价金额
            param.put("total_fee", totalFee);
            //终端IP
            param.put("spbill_create_ip", "127.0.0.1");
            //通知地址
            param.put("notify_url", notifyurl);
            //交易类型
            param.put("trade_type", "NATIVE");

            //2. 发送请求到微信支付系统；
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发送到微信支付系统 统一下单 的数据为：" + signedXml);

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setXmlParam(signedXml);
            httpClient.isHttps();
            httpClient.post();

            //3. 处理返回结果
            String content = httpClient.getContent();
            System.out.println("发送到微信支付系统 统一下单 的返回数据为：" + content);

            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            returnMap.put("outTradeNo", outTradeNo);
            returnMap.put("totalFee", totalFee);
            returnMap.put("result_code", resultMap.get("result_code"));
            returnMap.put("code_url", resultMap.get("code_url"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    @Override
    public Map<String, String> queryPayStatus(String outTradeNo) {
        try {
            //1. 封装微信需要的参数；
            Map<String, String> param = new HashMap<>();
            //公众账号ID
            param.put("appid", appid);
            //商户号
            param.put("mch_id", partner);
            //随机字符串；可以利用微信提供的工具类生成
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；可以在发送的时候利用工具类生成
            //param.put("sign", null);
            //商户订单号
            param.put("out_trade_no", outTradeNo);

            //2. 发送请求到微信支付系统；
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发送到微信支付系统 查询订单 的数据为：" + signedXml);

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setXmlParam(signedXml);
            httpClient.isHttps();
            httpClient.post();

            //3. 处理返回结果
            String content = httpClient.getContent();
            System.out.println("发送到微信支付系统 查询订单 的返回数据为：" + content);

            return WXPayUtil.xmlToMap(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
