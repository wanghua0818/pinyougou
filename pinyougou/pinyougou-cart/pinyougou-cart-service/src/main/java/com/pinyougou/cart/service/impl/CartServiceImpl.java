package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.cart.service.CartService;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    private static final String REDIS_CART_LIST = "cart_list";
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        // cartList = new ArrayList<>();
//        1. 判断是否是否存在和状态是已启用；
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        if (tbItem == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"1".equals(tbItem.getStatus())) {
            throw new RuntimeException("商品状态不合法");
        }

        String sellerId = tbItem.getSellerId();

        Cart cart = findCartBySellerId(cartList, sellerId);
//        2. 商家(cart)不存在；创建一个商家（cart）添加购买的商品到其商品列表；
        if (cart == null) {
            if (num > 0) {
                cart = new Cart();
                cart.setSellerId(sellerId);
                cart.setSellerName(tbItem.getSeller());

                List<TbOrderItem> orderItemList = new ArrayList<>();
                TbOrderItem orderItem = createOrderItem(tbItem, num);
                orderItemList.add(orderItem);
                cart.setOrderItemList(orderItemList);
                cartList.add(cart);
            } else {
                throw new RuntimeException("购买数量不合法");
            }
        } else {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem tbOrderItem = findOrderItemByItemId(orderItemList, itemId);
//       3. 商家（cart）存在；商品(tbOrderItem)存在的话；那么购买数量叠加并更新总价；
            if (tbOrderItem != null) {
                //购买数量叠加
                tbOrderItem.setNum(tbOrderItem.getNum() + num);
                //更新总价
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getPrice().doubleValue() * tbOrderItem.getNum()));
                //如果购买数量小于0,则需要将该商品从购物车删除
                if (tbOrderItem.getNum() < 1) {
                    cart.getOrderItemList().remove(tbOrderItem);
                }//如果商家没有任何产品,则将该商家从购物车移除
                if (orderItemList.size() <= 0) {
                    cartList.remove(cart);
                }
            } else {
                //4. 商家（cart）存在；商品(tbOrderItem)不存在的话；重新创建商品加入商品列表
                if (num > 0) {
                    tbOrderItem = createOrderItem(tbItem, num);
                    cart.getOrderItemList().add(tbOrderItem);
                } else {
                    throw new RuntimeException("购买商品数量不合理");
                }
            }
        }
        return cartList;
    }

    //已登录,根据用户名 从redis取出购物车列表
    @Override
    public List<Cart> findCartListByUsername(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_LIST).get(username);
        if (cartList != null) {
            return cartList;
        }
        return new ArrayList<>();
    }

    //保存购物车列表到Redis中
    @Override
    public void saveCartListInRedisByUsername(List<Cart> newCartList, String username) {
        redisTemplate.boundHashOps(REDIS_CART_LIST).put(username, newCartList);

    }

    //合并购物车
    @Override
    public List<Cart> mergeCartList(List<Cart> cookieCartList, List<Cart> redisCartList) {

        for (Cart cart : cookieCartList) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
                addItemToCartList(redisCartList, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return redisCartList;
    }

    //根据商品id 查询 购物车是否存在该商品
    private TbOrderItem findOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().equals(itemId)) {
                return orderItem;
            }
        }
        return null;
    }

    //构建购物车商品明细
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setNum(num);
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setPicPath(item.getImage());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }

    //根据商家id 查找购物车是否存在该商家
    private Cart findCartBySellerId(List<Cart> cartList, String sellerId) {
        if (cartList != null && cartList.size() > 0) {
            for (Cart cart : cartList) {
                if (cart.getSellerId().equals(sellerId)) {
                    return cart;
                }
            }
        }
        return null;
    }
}
