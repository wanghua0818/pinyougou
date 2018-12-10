package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/brand")
@RestController
public class BrandController {
    @Reference
    private BrandService brandService;

    @GetMapping("/findAll")
    public List<TbBrand> findAll() {
        List<TbBrand> brandList = brandService.queryAll();
        return brandList;
    }

    /**
     * http://localhost:9100/brand/testPage.do?page=1&rows=5
     * 分页查询品牌的第一页，每页五条数据
     */
    @GetMapping("/testPage")
    public List<TbBrand> testPage(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows) {
        return brandService.testPage(page, rows);
    }
}
