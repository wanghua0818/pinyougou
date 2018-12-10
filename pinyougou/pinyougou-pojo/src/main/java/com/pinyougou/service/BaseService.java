package com.pinyougou.service;

import com.pinyougou.vo.PageResult;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T>  {
    /**
     * 根据主键查询
     *
     * @param id
     * @return 实体类对象
     */
    T findOne(Serializable id);

    /**
     * 查询全部
     *
     * @return 实体类对象集合
     */
    List<T> findAll();

    /**
     * 根据条件查询
     *
     * @param t
     * @return
     */
    List<T> findByWhere(T t);

    /**
     * 根据分页信息查询
     *
     * @param page
     * @param rows
     * @return 分页结果对象
     */
    PageResult findPage(Integer page, Integer rows);

    /**
     * 根据分页信息条件查询
     *
     * @param page
     * @param rows
     * @param t
     * @return 分页结果对象
     */
    PageResult findPage(Integer page, Integer rows, T t);

    /**
     * 新增
     *
     * @param t 新增对象
     */
    void add(T t);

    /**
     * 更新
     *
     * @param t 更新条件
     */
    void update(T t);

    /**
     * 批量删除
     *
     * @param ids id数组
     */
    void deleteByIds(Serializable[] ids);
}
