package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/test")
@RestController
public class PageTestController {
    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;
    @Reference
    private GoodsService goodsService;
    @Reference
    private ItemCatService itemCatService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @GetMapping("/audit")
    public String audit(Long[] goodsIds) {
        for (Long goodsId : goodsIds) {
            genItemHtml(goodsId);
        }
        return "success";
    }
    //生成

    private void genItemHtml(Long goodsId) {
        try {
//            获取模板
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
//            获取模板需要的数据
            Map<String, Object> dataModel = new HashMap<>();
            Goods goods = goodsService.findGoodsById(goodsId, "1");
            dataModel.put("goods", goods.getGoods());
            dataModel.put("goodsDesc", goods.getGoodsDesc());
            dataModel.put("itemList", goods.getItemList());
            //查询三级商品分类
            TbItemCat itemCat1 =
                    itemCatService.findOne(goods.getGoods().getCategory1Id());
            dataModel.put("itemCat1", itemCat1.getName());
            TbItemCat itemCat2 =
                    itemCatService.findOne(goods.getGoods().getCategory2Id());
            dataModel.put("itemCat2", itemCat2.getName());
            TbItemCat itemCat3 =
                    itemCatService.findOne(goods.getGoods().getCategory3Id());
            dataModel.put("itemCat3", itemCat3.getName());
            //输出到指定路径
            String path = ITEM_HTML_PATH + goodsId + ".html";
            FileWriter fileWriter = new FileWriter(path);
            template.process(dataModel, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //移除
    @GetMapping("/delete")
    public String delete(Long[] ids) {
        for (Long goodsId : ids) {
            String pathName = ITEM_HTML_PATH + goodsId + ".html";
            File file = new File(pathName);
            if (file.exists()) {
                file.delete();
            }
        }
        return "success";
    }
}

