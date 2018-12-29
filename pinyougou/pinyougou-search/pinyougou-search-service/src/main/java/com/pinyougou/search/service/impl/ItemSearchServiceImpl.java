package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
        Criteria criteria = null;
        //处理空格
        if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
            searchMap.put("keywords", searchMap.get("keywords").toString().replaceAll(" ", ""));
            //设置查询条件
            criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        } else {
            criteria = new Criteria("item_keywords");
        }
        SimpleHighlightQuery query = new SimpleHighlightQuery();

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
        //设置分页信息
        //页号
        int pageNo = 1;
        if (!StringUtils.isEmpty(searchMap.get("pageNo"))) {
            pageNo = Integer.parseInt(searchMap.get("pageNo").toString());
        }
        //页大小
        int pageSize = 20;
        if (!StringUtils.isEmpty(searchMap.get("pageSize"))) {
            pageSize = Integer.parseInt(searchMap.get("pageSize").toString());
        }
        query.setOffset((pageNo - 1) * pageSize);
        query.setRows(pageSize);
        //处理排序
        if (!StringUtils.isEmpty(searchMap.get("sortField")) && !StringUtils.isEmpty(searchMap.get("sort"))) {
            //排序的域
            String sortField = searchMap.get("sortField").toString();
            //排序的顺序
            String sortOrder = searchMap.get("sort").toString();
            Sort sort = new Sort("DESC".equals(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, "item_" + sortField);
            query.addSort(sort);
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
        resultMap.put("totalPages", highlightPage.getTotalPages());
        resultMap.put("total", highlightPage.getTotalElements());
        return resultMap;
    }

    /**
     * 导入更新后的商品列表到solr中
     *
     * @param itemList
     */
    @Override
    public void importItemList(List<TbItem> itemList) {
        for (TbItem tbItem : itemList) {
            Map specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(specMap);
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }


     /* 根据商品id集合删除对应其在solr索引库的数据
     *
     * @param goodsIdList
     */
    @Override
    public void deleteItemListByGoodsIdList(List<Long> goodsIdList) {
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        SimpleQuery query = new SimpleQuery(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
