package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import javafx.scene.input.InputMethodTextRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service(interfaceClass = ItemSearchService.class)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        // SimpleQuery query = new SimpleQuery();
        //创建高亮搜索对象

        //处理空格
        if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
            searchMap.put("keywords", searchMap.get("keywords").toString().replaceAll(" ", ""));
        }
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //设置查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);

        //设置过滤查询条件
        //1.商品分类查询条件
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            SimpleFilterQuery categoryFilterQuery = new SimpleFilterQuery(categoryCriteria);
            query.addFilterQuery(categoryFilterQuery);
        }
        //2.品牌分类查询条件
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            SimpleFilterQuery brandFilterQuery = new SimpleFilterQuery(brandCriteria);
            query.addFilterQuery(brandFilterQuery);
        }
        //3.商品规格查询条件
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                Criteria specCriteria = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
                SimpleFilterQuery specFilterQuery = new SimpleFilterQuery(specCriteria);
                query.addFilterQuery(specFilterQuery);
            }
        }
        //价格过滤查询
        if (!StringUtils.isEmpty(searchMap.get("price"))) {
            String[] prices = searchMap.get("price").toString().split("-");
            //创建过滤查询对象
            Criteria startCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
            SimpleFilterQuery smallSimpleFilterQuery = new SimpleFilterQuery(startCriteria);
            query.addFilterQuery(smallSimpleFilterQuery);
            if (!"*".equals(prices[1])) {
                Criteria endCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                SimpleFilterQuery endSimpleFilterQuery = new SimpleFilterQuery(endCriteria);
                query.addFilterQuery(endSimpleFilterQuery);
            }
        }
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //处理高亮标题
        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
        if (highlighted != null && highlighted.size() > 0) {
            for (HighlightEntry<TbItem> entry : highlighted) {
                List<HighlightEntry.Highlight> highlights = entry.getHighlights();
                //第一个get(0)获取第一个域
                //第二个get(0)获取该域的第一个字符串
                if (highlights != null && highlights.size() > 0 && highlights.get(0).getSnipplets() != null &&
                        highlights.get(0).getSnipplets().size() > 0) {
                    String title = highlights.get(0).getSnipplets().get(0);
                    entry.getEntity().setTitle(title);
                }
            }
        }
        //设置返回的商品列表
        resultMap.put("rows", highlightPage.getContent());
        return resultMap;
    }
}
