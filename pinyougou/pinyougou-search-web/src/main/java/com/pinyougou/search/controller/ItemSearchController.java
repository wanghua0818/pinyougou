package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/itemSearch")
@RestController
public class ItemSearchController {
    @Reference
    private ItemSearchService itemSearchService;
    /**
     *根据条件搜索
     * @param searchMap
     * @return
     */
    @PostMapping("/search")
    public Map<String,Object> search(@RequestBody Map<String,String> searchMap){
        return itemSearchService.search(searchMap);
    }
}