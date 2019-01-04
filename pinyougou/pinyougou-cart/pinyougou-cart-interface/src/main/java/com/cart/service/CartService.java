package com.cart.service;

import com.pinyougou.vo.Cart;

import java.util.List;

public interface CartService {

    List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num);

    List<Cart> findCartListByUsername(String username);

    void saveCartListInRedisByUsername(List<Cart> newCartList, String username);

    List<Cart> mergeCartList(List<Cart> cookieCartList, List<Cart> redisCartList);
}
