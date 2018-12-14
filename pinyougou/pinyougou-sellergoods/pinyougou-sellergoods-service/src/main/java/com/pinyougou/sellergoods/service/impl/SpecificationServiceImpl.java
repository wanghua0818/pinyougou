package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.sellergoods.service.SpecificationService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = SpecificationService.class)
public class SpecificationServiceImpl extends BaseServiceImpl<TbSpecification> implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbSpecification specification) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(specification.getSpecName())) {
            criteria.andLike("specName", "%" + specification.getSpecName() + "%");
        }

        List<TbSpecification> list = specificationMapper.selectByExample(example);
        PageInfo<TbSpecification> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void add(Specification specification) {
        //新增规格
        specificationMapper.insertSelective(specification.getSpecification());
        //新增选项规格
        if (specification.getSpecificationOptionList() != null && specification.getSpecificationOptionList().size() > 0) {
            List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
            for (TbSpecificationOption tbSpecificationOption : specificationOptionList) {
                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
                specificationOptionMapper.insertSelective(tbSpecificationOption);
            }
        }
    }

    @Override
    public Specification findOne(Long id) {
        Specification specification = new Specification();
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        specification.setSpecification(tbSpecification);
        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(id);
        List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.select(param);
        specification.setSpecificationOptionList(specificationOptionList);
        return specification;
    }

    @Override
    public void update(Specification specification) {
        //更新规格
        specificationMapper.updateByPrimaryKeySelective(specification.getSpecification());
        //删除原规格选项
        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(specification.getSpecification().getId());
        specificationOptionMapper.delete(param);
        //新增规格选项
        List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
        if (specificationOptionList != null && specificationOptionList.size() > 0) {
            for (TbSpecificationOption tbSpecificationOption : specificationOptionList) {
                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
                specificationOptionMapper.insertSelective(tbSpecificationOption);
            }

        }
    }

    @Override
    public void deleteSpecificationByIds(Long[] ids) {
        deleteByIds(ids);
        Example example = new Example(TbSpecificationOption.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("specId", Arrays.asList(ids));
        specificationOptionMapper.deleteByExample(example);
    }

    @Override
    public List<Map<String, String>> selectOptionList() {

        return specificationMapper.selectOptionList();
    }
}
