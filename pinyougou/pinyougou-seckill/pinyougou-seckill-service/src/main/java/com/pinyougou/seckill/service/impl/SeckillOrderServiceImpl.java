package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.common.util.RedisLock;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.apache.http.impl.execchain.TunnelRefusedException;
import org.aspectj.weaver.IWeaveRequestor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service(interfaceClass = SeckillOrderService.class)
public class SeckillOrderServiceImpl extends BaseServiceImpl<TbSeckillOrder> implements SeckillOrderService {
    //秒杀商品订单在redis中的key的名称
    private static final String SECKILL_ORDERS = "SECKILL_ORDERS";
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;

    @Override
    public PageResult search(Integer page, Integer rows, TbSeckillOrder seckillOrder) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(seckillOrder.get***())){
            criteria.andLike("***", "%" + seckillOrder.get***() + "%");
        }*/

        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        PageInfo<TbSeckillOrder> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 根据秒杀商品id生成秒杀订单
     *
     * @param seckillId 秒杀商品id
     * @param username  用户id
     * @return 操作结果
     */
    @Override
    public String submitOrder(String username, Long seckillId) throws InterruptedException {
        //加分布式锁
        RedisLock redisLock = new RedisLock(redisTemplate);
        if (redisLock.lock(seckillId.toString())) {
            //1.判断商品存在并且商品库存大于0
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).get(seckillId);
            if (seckillGoods == null) {
                throw new RuntimeException("秒杀商品不存在");
            }
            if (seckillGoods.getStockCount() <= 0) {
                throw new RuntimeException("商品已经秒杀完!!");
            }
            //2.将商品的库存减1
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            if (seckillGoods.getStockCount() > 0) {
                //库存大于0,更新redis中商品缓存
                redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).put(seckillId, seckillGoods);
            } else {
                //库存等于0;更新秒杀商品到mysql,删除redis中的秒杀商品
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //删除对应的redis缓存
                redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).delete(seckillId);
            }
            //释放分布式锁
            redisLock.unlock(seckillId.toString());
            //生存订单并保存到redis中
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            Long orderId = idWorker.nextId();
            seckillOrder.setId(orderId);
            seckillOrder.setCreateTime(new Date());
            // 秒杀价钱
            seckillOrder.setMoney(seckillGoods.getCostPrice());
            seckillOrder.setSeckillId(seckillId);
            seckillOrder.setUserId(username);
            seckillOrder.setStatus("0");//未支付
            seckillOrder.setSellerId(seckillGoods.getSellerId());
            redisTemplate.boundHashOps(SECKILL_ORDERS).put(seckillOrder.getId().toString(), seckillOrder);

            //返回订单号
            return seckillOrder.getId().toString();
        }
        return null;
    }

    @Override
    public TbSeckillOrder findSeckillOrderInRedisByOutTradeNo(String outTradeNo) {
        TbSeckillOrder tbSeckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps(SECKILL_ORDERS).get(outTradeNo);

        return tbSeckillOrder;
    }

    @Override
    public void saveSeckillOrderInRedisToDb(String outTradeNo, String transaction_id) {
        TbSeckillOrder seckillOrder = findSeckillOrderInRedisByOutTradeNo(outTradeNo);
        seckillOrder.setStatus("1");//已支付
        seckillOrder.setPayTime(new Date());
        seckillOrder.setTransactionId(transaction_id);
        //保存到 mysql中去
        seckillOrderMapper.insertSelective(seckillOrder);
        //将订单从redis删除
        redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);

    }

    /**
     * 将redis中的订单删除并更新秒杀商品剩余库存
     *
     * @param outTradeNo 订单号
     */
    @Override
    public void removeSeckillOrderInRedis(String outTradeNo) throws InterruptedException {
        //判断订单是否存在
        TbSeckillOrder order = (TbSeckillOrder) redisTemplate.boundHashOps(SECKILL_ORDERS).get(outTradeNo);
        if (order != null) {
            redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);
            //加分布式锁
            RedisLock redisLock = new RedisLock(redisTemplate);
            if (redisLock.lock(order.getSeckillId().toString())) {
                TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).get(order.getSeckillId());
                if (seckillGoods == null) {
                    //如果在redis不存在,则从数据库找回来 并加回库存设置到redis中
                    seckillGoods = seckillGoodsMapper.selectByPrimaryKey(order.getSeckillId());
                }
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
                redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).put(seckillGoods.getId(), seckillGoods);
                //释放分布式锁
                redisLock.unlock(order.getSeckillId().toString());
            }


        }
    }
}
