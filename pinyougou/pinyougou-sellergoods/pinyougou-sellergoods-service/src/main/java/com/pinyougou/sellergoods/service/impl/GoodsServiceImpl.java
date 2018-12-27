package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

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
        criteria.andNotEqualTo("isDelete", "1");
        //商家限定
        if (!StringUtils.isEmpty(goods.getSellerId())) {
            criteria.andEqualTo("sellerId", goods.getSellerId());
        }
        if (!StringUtils.isEmpty(goods.getGoodsName())) {
            criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
        }
        if (!StringUtils.isEmpty(goods.getAuditStatus())) {
            criteria.andEqualTo("auditStatus", goods.getAuditStatus());
        }

        List<TbGoods> list = goodsMapper.selectByExample(example);
        PageInfo<TbGoods> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }


    @Override
    public Goods findGoods(Long id) {

        return findGoodsById(id, null);
    }

    public void updateGoods(Goods goods) {
        //更新商品信息
        goods.getGoods().setAuditStatus("0");//更新过后 重新设置为未审核
        goodsMapper.updateByPrimaryKeySelective(goods.getGoods());
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());
        //删除原有sku列表
        TbItem param = new TbItem();
        param.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(param);
        //保存新的sku列表
        saveItemLit(goods);
    }

    /**
     * 根据id更新状态
     *
     * @param ids
     * @param status
     * @return
     */
    @Override
    public void updateByIdsAndStatus(Long[] ids, String status) {
        TbGoods goods = new TbGoods();
        goods.setAuditStatus(status);
        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        goodsMapper.updateByExampleSelective(goods, example);
        //不仅要修改商品的审核状态为审核通过而且需要将这些spu id对应的那些sku的状态修改为已启用（1）。
        if ("2".equals(status)) {
            //根据商品spu id数组修改对于的sku 的状态为1
            //update tb_item set status=1 where goods_id in (?,?,,,);
            TbItem item = new TbItem();
            item.setStatus("1");

            Example itemExample = new Example(TbItem.class);
            itemExample.createCriteria().andIn("goodsId", Arrays.asList(ids));

            itemMapper.updateByExampleSelective(item, itemExample);
        }
    }

    @Override
    public void deleteGoodsByIds(Long[] ids) {
        TbGoods param = new TbGoods();
        param.setIsDelete("1");
        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        goodsMapper.updateByExampleSelective(param, example);
    }

    /**
     * 更新上下架
     *
     * @param ids
     * @param isMarketable
     */
    public void updateByIdsAndIsMarketable(Long[] ids, String isMarketable) {
        TbGoods goods = new TbGoods();
        goods.setIsMarketable(isMarketable);
        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        goodsMapper.updateByExampleSelective(goods, example);
    }

    @Override
    public List<TbItem> findItemGoodsByGoodsIdAndStatus(Long[] ids, String s) {
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", s);
        criteria.andIn("goodsId", Arrays.asList(ids));
        List<TbItem> itemList = itemMapper.selectByExample(example);
        return itemList;
    }

    @Override
    public Goods findGoodsById(Long id, String status) {
        Goods goods = new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(goodsDesc);
        //查询商品sku列表
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("goodsId", id);
        if (!StringUtils.isEmpty(status)) {
            criteria.andEqualTo("status", status);
        }
        example.orderBy("isDefault").desc();
        List<TbItem> tbItemList = itemMapper.selectByExample(example);
        goods.setItemList(tbItemList);
        return goods;
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
