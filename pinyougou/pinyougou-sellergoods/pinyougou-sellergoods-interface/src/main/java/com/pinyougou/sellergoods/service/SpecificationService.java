package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Specification;

public interface SpecificationService extends BaseService<TbSpecification> {

    PageResult search(Integer page, Integer rows, TbSpecification specification);

    void add(Specification specification);

    /**
     * 通过id查询得到规格和规格选项列表
     * @param id
     * @return
     */
    Specification findOne(Long id);

    void update(Specification specification);

    void deleteSpecificationByIds(Long[] ids);

}