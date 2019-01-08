package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/seckillOrder")
@RestController
public class SeckillOrderController {

    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/findAll")
    public List<TbSeckillOrder> findAll() {
        return seckillOrderService.findAll();
    }

    //  return $http.get("../seckillOrder/submitOrder.do?seckillId=" + id);

    /**
     * @param seckillId 秒杀商品id
     * @return
     */
    @RequestMapping("/submitOrder")
    public Result submitOrder(Long seckillId) {

        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(username)) {
                return Result.fail("先请登录!!在抢购");
            }
            //根据 秒杀商品id,生成秒杀商品订单,并将订单暂只存redis中
            String outTradeNo = seckillOrderService.submitOrder(username, seckillId);
            if (outTradeNo != null) {
                return Result.ok(outTradeNo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("提交订单失败");
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return seckillOrderService.findPage(page, rows);
    }

    @PostMapping("/add")
    public Result add(@RequestBody TbSeckillOrder seckillOrder) {
        try {
            seckillOrderService.add(seckillOrder);
            return Result.ok("增加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("增加失败");
    }

    @GetMapping("/findOne")
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderService.findOne(id);
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbSeckillOrder seckillOrder) {
        try {
            seckillOrderService.update(seckillOrder);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            seckillOrderService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 分页查询列表
     *
     * @param seckillOrder 查询条件
     * @param page         页号
     * @param rows         每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody TbSeckillOrder seckillOrder, @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return seckillOrderService.search(page, rows, seckillOrder);
    }

}
