package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;

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
            return Result.ok("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("更新失败");
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
    public Result add(@RequestBody Goods goods) {
        try {
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            //获取商家id
            goods.getGoods().setSellerId(sellerId);
            //设为未审核
            goods.getGoods().setAuditStatus("0");
            goodsService.addGoods(goods);
            return Result.ok("商品增加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("商品增加失败");
    }

    @GetMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findGoods(id);
    }

    /**
     * return $http.post("../goods/update.do",entity);
     *
     * @param goods
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            //校验商家
            TbGoods oldGoods = goodsService.findOne(goods.getGoods().getId());
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (oldGoods.getSellerId().equals(sellerId) && sellerId.equals(goods.getGoods().getSellerId())) {
                goodsService.updateGoods(goods);
                return Result.ok("修改成功");
            } else {
                return Result.ok("违法修改");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    /**
     * return $http.get("../goods/updateMarketable.do?ids=" + selectedIds + "&isMarketable=+ isMarketable")
     * @param Long[] ids, String isMarketable)
     * @return
     */
    @GetMapping("/updateMarketable")
    public Result updateMarketable(Long[] ids, String isMarketable) {
            try {
                goodsService.updateByIdsAndIsMarketable(ids, isMarketable);
                return Result.ok("更新成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Result.fail("更新失败");
        }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
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

            //商家只能看到和查询自己的商品
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.setSellerId(sellerId);
            return goodsService.search(page, rows, goods);
    }

}
