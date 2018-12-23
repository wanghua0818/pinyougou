package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = ItemSearchService.class)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        // SimpleQuery query = new SimpleQuery();
        //创建高亮搜索对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //处理高亮标题
        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
        if (highlighted != null && highlighted.size() > 0) {
            for (HighlightEntry<TbItem> entry : highlighted) {
                List<HighlightEntry.Highlight> highlights = entry.getHighlights();
                //第一个get(0)获取第一个域
                //第二个get(0)获取该域的第一个字符串
               if (highlights!=null && highlights.size()>0 && highlights.get(0).getSnipplets()!=null&&
                       highlights.get(0).getSnipplets().size()>0){
                   String title = highlights.get(0).getSnipplets().get(0).toString();
                   entry.getEntity().setTitle(title);
               }
            }
        }
        //设置返回的商品列表
        resultMap.put("rows", highlightPage.getContent());
        return resultMap;
    }
}
