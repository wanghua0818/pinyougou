package com.pinyougou.item.activemq.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class ItemAuditMessageListener extends AbstractAdaptableMessageListener {
    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;
    @Reference
    private GoodsService goodsService;
    @Reference
    private ItemCatService itemCatService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        ObjectMessage objectMessage = (ObjectMessage) message;
        Long[] ids = (Long[]) objectMessage.getObject();
        for (Long id : ids) {
            genItemHtml(id);
        }
    }

    private void genItemHtml(Long id) {
        try {
            //获取模板
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            //获取模板需要的数据
            Map<String, Object> dataModel = new HashMap<>();
            Goods goods = goodsService.findGoodsById(id, "1");
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
            String path = ITEM_HTML_PATH + id + ".html";
            FileWriter fileWriter = new FileWriter(path);
            template.process(dataModel, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
