package com.itheima.solr;

import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:spring/applicationContext-solr.xml"})
public class SolrTest {
    @Autowired
    private SolrTemplate solrTemplate;

    //新增更新
    @Test
    public void testSolr() {
        TbItem item = new TbItem();
        item.setId(1L);
        item.setTitle("一加手机6T 8GB+128GB 亮瓷黑 光感屏幕指纹 全面屏双摄游戏手机 全网通4G 双卡双待");
        item.setBrand("一加");
        item.setPrice(new BigDecimal(3888));
        item.setGoodsId(123L);
        item.setSeller("中兴旗舰店");
        item.setCategory("手机");
        solrTemplate.saveBean(item);
        solrTemplate.commit();
    }

    //根据主键删除
    @Test
    public void deleteByIdSolr() {
        solrTemplate.deleteById("1");
        solrTemplate.commit();
    }

    //根据条件删除
    @Test
    public void deleteByQuery() {
        SimpleQuery query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //根据关键字分页查询
    @Test
    public void testQueryInPage() {
        SimpleQuery query = new SimpleQuery("*:*");
        query.setOffset(0);//分页起始号，默认为0
        query.setRows(10);//分页页大小；默认为10
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);
        showPage(scoredPage);
    }

    public void showPage(ScoredPage<TbItem> scoredPage) {
        System.out.println("总记录数为:" + scoredPage.getTotalElements());
        System.out.println("总页数数为:" + scoredPage.getTotalPages());
        List<TbItem> itemList = scoredPage.getContent();
        for (TbItem tbItem : itemList) {
            System.out.println(tbItem.getCategory());
        }
    }
    //多条件查询
    @Test
    public void testMultiQuery(){
        SimpleQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_title").contains("一");
        query.addCriteria(criteria);
        Criteria criteria2 = new Criteria("item_price").contains("3888");
        query.addCriteria(criteria);
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);
        showPage(scoredPage);
    }

}
