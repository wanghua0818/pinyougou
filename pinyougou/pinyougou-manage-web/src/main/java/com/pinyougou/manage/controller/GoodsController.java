package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.*;

import javax.jms.*;
import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ActiveMQQueue itemSolrQueue;
    @Autowired
    private ActiveMQQueue itemDeleteSolrQueue;
    @Autowired
    private ActiveMQTopic itemTopic;
    @Autowired
    private ActiveMQTopic itemDeleteTopic;


    /**
     * return $http.get("../goods/updateStatus.do?ids=" + selectedIds + "&status=" + status);
     * 更新状态
     *
     * @param ids
     * @param status
     * @return
     */
    @GetMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateByIdsAndStatus(ids, status);
            if ("2".equals(status)) {
                //如果审核通过则需要更新solr索引库数据
                //查询需要更新的商品列表
                List<TbItem> itemList = goodsService.findItemGoodsByGoodsIdAndStatus(ids, "1");
                //itemSearchService.importItemList(itemList);
                jmsTemplate.send(itemSolrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage();
                        System.out.println("=================================================================");
                        System.out.println(JSON.toJSONString(itemList));
                        System.out.println("=================================================================");
                        textMessage.setText(JSON.toJSONString(itemList));

                        return textMessage;
                    }
                });
                //发送商品审核通过的消息
                sendMessage(itemTopic, ids);

            }
            return Result.ok("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("更新失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            //逻辑删除 ，把商品状态改为删除状态
            goodsService.deleteGoodsByIds(ids);
            // 删除成功,从solr索引库中删除对应的商品列表
            //itemSearchService.deleteItemListByGoodsIdList(Arrays.asList(ids));
            sendMessage(itemDeleteSolrQueue, ids);

            //发送商品删除的消息
            sendMessage(itemDeleteTopic, ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    private void sendMessage(Destination destination, Long[] ids) throws JMSException {
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage();
                objectMessage.setObject(ids);
                return objectMessage;
            }
        });
    }

    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return goodsService.findPage(page, rows);
    }

    @PostMapping("/add")
    public Result add(@RequestBody TbGoods goods) {
        try {
            goodsService.add(goods);
            return Result.ok("增加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("增加失败");
    }

    @GetMapping("/findOne")
    public TbGoods findOne(Long id) {
        return goodsService.findOne(id);
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbGoods goods) {
        try {
            goodsService.update(goods);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }


    /**
     * 分页查询列表
     *
     * @param goods 查询条件
     * @param page  页号
     * @param rows  每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return goodsService.search(page, rows, goods);
    }

}
