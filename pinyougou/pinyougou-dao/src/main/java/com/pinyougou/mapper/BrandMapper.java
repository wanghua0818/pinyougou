package com.pinyougou.mapper;

import com.pinyougou.pojo.TbBrand;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<TbBrand> {
    /**
     * 查询所有品牌
     * @return
     */
    List<TbBrand> queryAll();

}
