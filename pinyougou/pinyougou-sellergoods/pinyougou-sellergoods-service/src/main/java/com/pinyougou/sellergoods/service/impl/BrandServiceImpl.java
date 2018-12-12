package com.pinyougou.sellergoods.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

//该service注解是dubbo的，不是spring的
@Service(interfaceClass = BrandService.class)
public class BrandServiceImpl extends BaseServiceImpl<TbBrand> implements BrandService {

    @Autowired
    private BrandMapper brandMapper;


    /**
     * 查询所有品牌
     *
     * @return
     */
    @Override
    public List<TbBrand> queryAll() {
        return brandMapper.queryAll();
    }

    @Override
    public List<TbBrand> testPage(Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        return brandMapper.selectAll();
    }

    @Override
    public PageResult search(TbBrand tbBrand, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        //设置查询条件
        Example example = new Example(TbBrand.class);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(tbBrand.getFirstChar())) {
            criteria.andEqualTo("firstChar", tbBrand.getFirstChar());
        }
        if (!StringUtils.isEmpty(tbBrand.getName())) {
            criteria.andLike("name", "%" + tbBrand.getName() + "%");
        }
        List<TbBrand> tbBrandList = brandMapper.selectByExample(example);
        PageInfo<TbBrand> pageInfo = new PageInfo<>(tbBrandList);
        PageResult pageResult = new PageResult(pageInfo.getTotal(), pageInfo.getList());
        return pageResult;
    }


}
