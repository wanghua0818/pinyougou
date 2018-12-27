package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.Goods;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ItemController {
    @Reference
    private GoodsService goodsService;
    //http://item.pinyougou.com/{{item.goodsId}}.html
    @RequestMapping("/{goodsId}")
    public ModelAndView toItemPages(@PathVariable Long goodsId) {
        //根据商品id 查询商品的基本信息,描述信息,启用的sku商品列表
        Goods goods = goodsService.findGoodsById(goodsId,"1");
        ModelAndView mav = new ModelAndView("item");
        TbGoods tbgoods = goodsService.findOne(goodsId);
        mav.addObject("itemCat1", tbgoods.getCategory1Id());
        mav.addObject("itemCat2", tbgoods.getCategory2Id());
        mav.addObject("itemCat3", tbgoods.getCategory3Id());
        mav.addObject("goods", goods.getGoods());
        mav.addObject("goodsDesc", goods.getGoodsDesc());
        mav.addObject("itemList", goods.getItemList());
        return mav;
    }
}
