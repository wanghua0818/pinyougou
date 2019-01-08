package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service(interfaceClass = SeckillGoodsService.class)
public  class SeckillGoodsServiceImpl extends BaseServiceImpl<TbSeckillGoods> implements SeckillGoodsService {

    public static final Object SECKILL_GOODS = "SECKILL_GOODS";
    @Autowired

    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult search(Integer page, Integer rows, TbSeckillGoods seckillGoods) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(seckillGoods.get***())){
            criteria.andLike("***", "%" + seckillGoods.get***() + "%");
        }*/

        List<TbSeckillGoods> list = seckillGoodsMapper.selectByExample(example);
        PageInfo<TbSeckillGoods> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * @return
     */
    @Override
    public List<TbSeckillGoods> findList() {
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(SECKILL_GOODS).values();
        if (seckillGoodsList == null || seckillGoodsList.size() == 0) {

            Example example = new Example(TbSeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //秒杀商品状态为1
            criteria.andEqualTo("status", 1);
            //库存大于0
            criteria.andGreaterThan("stockCount", 0);
            //开始时间小于或者等于当前时间
            criteria.andLessThanOrEqualTo("startTime", new Date());
            //结束时间大于当前时间
            criteria.andGreaterThan("endTime", new Date());
            example.orderBy("startTime");
            seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            //将秒杀商品一个一个存入redis中
            if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
                for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                    redisTemplate.boundHashOps(SECKILL_GOODS).put(seckillGoods.getId(), seckillGoods);
                }
            }

        }
        return seckillGoodsList;
    }

    /**
     * 根据秒杀商品id 在Redis缓存中 查找商品信息
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillGoods findSecKillGoodsById(Long id) {
        TbSeckillGoods tbSeckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS).get(id);
        return tbSeckillGoods;
    }

}
