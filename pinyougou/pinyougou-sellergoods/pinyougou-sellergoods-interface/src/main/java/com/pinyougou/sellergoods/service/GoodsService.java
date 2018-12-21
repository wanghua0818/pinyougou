package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;

import java.util.List;

public interface GoodsService extends BaseService<TbGoods> {

    PageResult search(Integer page, Integer rows, TbGoods goods);

    void addGoods(Goods goods);

    Goods findGoods(Long id);

    void updateGoods(Goods goods);

    void updateByIdsAndStatus(Long[] ids, String status);

    void deleteGoodsByIds(Long[] ids);

    void updateByIdsAndIsMarketable(Long[] ids, String isMarketable);
}