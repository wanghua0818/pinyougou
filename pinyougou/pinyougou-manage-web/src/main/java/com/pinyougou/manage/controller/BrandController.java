package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/brand")
@RestController
public class BrandController {
    @Reference
    private BrandService brandService;

    /**
     * 加载select2品牌列表
     * 格式：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
     *
     * @return
     */
    @GetMapping("/selectOptionList")
    public List<Map<String, String>> selectOptionList() {
       return brandService.selectOptionList();
    }

    @GetMapping("/findAll")
    public List<TbBrand> findAll() {
        /*List<TbBrand> brandList = brandService.queryAll();
        return brandList;*/
        return brandService.findAll();
    }

    /**
     * http://localhost:9100/brand/testPage.do?page=1&rows=5
     * 分页查询品牌的第一页，每页五条数据
     */
    @GetMapping("/testPage")
    public List<TbBrand> testPage(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows) {
        // return brandService.testPage(page, rows);
        return (List<TbBrand>) brandService.findPage(page, rows).getRows();
    }

    /**
     * ../brand/findPage.do?page=" + page + "&rows=" + rows
     */
   /* @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return brandService.findPage(page, rows);
    }*/

    /**
     * 保存品牌
     *
     * @param tbBrand
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbBrand tbBrand) {
        System.out.println(tbBrand.getName());
        System.out.println(tbBrand.getFirstChar());
        try {
            if (tbBrand.getName() != null && tbBrand.getFirstChar() != null) {
                brandService.add(tbBrand);
                return Result.ok("新增品牌成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增品牌失败,请检查品牌格式或者网络问题");
    }

    //../brand/findOne.do
    @GetMapping("/findOne")
    public TbBrand findOne(Long id) {
        TbBrand tbBrand = brandService.findOne(id);
        return tbBrand;
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand) {
        try {
            brandService.update(tbBrand);
            return Result.ok("修改数据成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改数据失败");
    }

    /**
     * 删除品牌
     *
     * @param ids 品牌id数组
     * @return Result结果信息对象
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            brandService.deleteByIds(ids);
            return Result.ok("删除品牌成功");
        }
        return Result.fail("删除品牌失败");
    }

    @PostMapping("/search")
    public PageResult search(@RequestBody TbBrand tbBrand, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows) {
        return brandService.search(tbBrand, page, rows);
    }
}
