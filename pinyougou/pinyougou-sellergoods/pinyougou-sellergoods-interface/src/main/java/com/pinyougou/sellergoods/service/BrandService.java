package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;

import java.util.List;

public interface BrandService {
    /**
     * 查询所有品牌
     * @return
     */
    List<TbBrand> queryAll();
}
