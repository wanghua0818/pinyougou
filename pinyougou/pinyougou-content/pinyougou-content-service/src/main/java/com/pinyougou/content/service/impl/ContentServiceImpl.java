package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service(interfaceClass = ContentService.class)
public class ContentServiceImpl extends BaseServiceImpl<TbContent> implements ContentService {

    private static final String REDIS_CONTENT = "content";
    @Autowired
    private ContentMapper contentMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增
     *
     * @param tbContent 新增对象
     */
    @Override
    public void add(TbContent tbContent) {
        super.add(tbContent);
        updateContentInRedisByCategoryId(tbContent.getCategoryId());
    }

    private void updateContentInRedisByCategoryId(Long categoryId) {
        try {
            redisTemplate.boundHashOps(REDIS_CONTENT).delete(categoryId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新
     *
     * @param tbContent 更新条件
     */
    @Override
    public void update(TbContent tbContent) {
        Long oldCategoryId = super.findOne(tbContent.getId()).getCategoryId();
        //更新之前的分类id
        super.update(tbContent);
        if (!oldCategoryId.equals(tbContent.getCategoryId())) {
            redisTemplate.boundHashOps(REDIS_CONTENT).delete(oldCategoryId);
        }
        redisTemplate.boundHashOps(REDIS_CONTENT).delete(tbContent.getCategoryId());
    }

    /**
     * 批量删除
     *
     * @param ids id数组
     */
    @Override
    public void deleteByIds(Serializable[] ids) {
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        List<TbContent> list = contentMapper.selectByExample(example);
        if (list != null && list.size() > 0) {
            for (TbContent tbContent : list) {
                updateContentInRedisByCategoryId(tbContent.getCategoryId());
            }
        }
        super.deleteByIds(ids);
    }

    @Override
    public PageResult search(Integer page, Integer rows, TbContent content) {
        PageHelper.startPage(page, rows);
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(content.get***())){
            criteria.andLike("***", "%" + content.get***() + "%");
        }*/
        List<TbContent> list = contentMapper.selectByExample(example);
        PageInfo<TbContent> pageInfo = new PageInfo<>(list);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {
        List<TbContent> list = null;
        try {
            list = (List<TbContent>) redisTemplate.boundHashOps(REDIS_CONTENT).get(categoryId);
            if (list != null) {
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", "1");
        criteria.andEqualTo("categoryId", categoryId);
        example.orderBy("sortOrder").desc();
        list = contentMapper.selectByExample(example);
        try {
            redisTemplate.boundHashOps(REDIS_CONTENT).put(categoryId, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
