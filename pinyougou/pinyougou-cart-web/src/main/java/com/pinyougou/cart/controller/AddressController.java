package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.user.service.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/address")
@RestController
public class AddressController {

    @Reference
    private AddressService addressService;

    /**
     * 根据用户id查找用户地址列表
     *
     * @return
     */
    @RequestMapping("/findAddress")
    public List<TbAddress> findAddress() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbAddress param = new TbAddress();
        param.setUserId(userId);
        List<TbAddress> addressList = addressService.findByWhere(param);
        return addressList;

    }
}
