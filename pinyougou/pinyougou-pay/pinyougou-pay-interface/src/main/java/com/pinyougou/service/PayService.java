package com.pinyougou.service;



import java.util.Map;


public interface PayService {
    Map<String, String> createNative(String outTradeNo, String totalFee);

    Map<String, String> queryPayStatus(String outTradeNo);
}
