package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Transactional
@Service(interfaceClass = GoodsService.class)
public class GoodsServiceImpl extends BaseServiceImpl<TbGoods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsDescMapper goodsDescMapper;
    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private SellerMapper sellerMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private ItemMapper itemMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbGoods goods) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(goods.get***())){
            criteria.andLike("***", "%" + goods.get***() + "%");
        }*/

        List<TbGoods> list = goodsMapper.selectByExample(example);
        PageInfo<TbGoods> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void addGoods(Goods goods) {
        //新增商品基本信息
        goodsMapper.insertSelective(goods.getGoods());
        //新增商品描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insertSelective(goods.getGoodsDesc());
        //新增sku商品列表
        saveItemLit(goods);
    }
    private void saveItemLit(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            if (goods.getItemList() != null && goods.getItemList().size() > 0) {
                for (TbItem item : goods.getItemList()) {
                    //设置标题
                    String title = goods.getGoods().getGoodsName();
                    Map<String, String> map = JSONArray.parseObject(item.getSpec(), Map.class);
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);
                    setItemValue(goods, item);
                    itemMapper.insertSelective(item);
                }
            }
        } else {
            //如果没有启动规格，则只存在一条 SKU 信息
            TbItem tbItem = new TbItem();
            tbItem.setTitle(goods.getGoods().getGoodsName());
            tbItem.setPrice(goods.getGoods().getPrice());
            tbItem.setNum(9999);
            tbItem.setStatus("0");
            tbItem.setIsDefault("1");
            tbItem.setSpec("{}");
            setItemValue(goods, tbItem);
            itemMapper.insertSelective(tbItem);
        }

    }

    private void setItemValue(Goods goods, TbItem item) {
        //设置商品id
        item.setGoodsId(goods.getGoods().getId());
        //sku的商品分类id
        item.setCategoryid(goods.getGoods().getCategory3Id());
        //sku 的商品分类 中文名称
        item.setCategory(itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id()).getName());
        //创建时间
        item.setCreateTime(new Date());
        //
        /*if (!StringUtils.isEmpty(goods.getGoodsDesc().getItemImages())) {
            List<Map> imageList = JSONArray.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
            item.setImage(imageList.get(0).get("url").toString());
        }*/
        //图片
        List<Map> imgList =
                JSONArray.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imgList != null && imgList.size() > 0) {
            //将商品的第一张图作为 sku 的图片
            item.setImage(imgList.get(0).get("url").toString());
        }
        //卖家id
        item.setSellerId(goods.getGoods().getSellerId());
        //卖家中文名字
        item.setSeller(sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId()).getName());
        //品牌名称
        item.setBrand(brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId()).getName());
        item.setUpdateTime(new Date());
    }
}
