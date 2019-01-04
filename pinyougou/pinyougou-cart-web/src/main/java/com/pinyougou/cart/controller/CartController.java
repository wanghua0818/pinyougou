package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.cart.service.CartService;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/cart")
@RestController
public class CartController {
    private static final String COOKIE_CART_LIST = "PYG_CART_LIST";
    private static final int COOKIE_CART_MAX_AGE = 3600 * 24;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Reference
    private CartService cartService;

    /**
     * 得到用户名
     *
     * @return
     */
    @GetMapping("/getUsername")
    public Map<String, Object> getUsername() {
        Map<String, Object> map = new HashMap<>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username", username);
        return map;
    }

    //加载购物车列表
    @GetMapping("/findCartList")
    public List<Cart> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            String cartListJsonStr = CookieUtils.getCookieValue(request, COOKIE_CART_LIST, true);
            List<Cart> cookieCartList = new ArrayList<>();
            if (!StringUtils.isEmpty(cartListJsonStr)) {
                cookieCartList = JSONArray.parseArray(cartListJsonStr, Cart.class);
            }
            //匿名登录的情况下,从cookie获取购物车数据
            if ("anonymousUser".equals(username)) {
                return cookieCartList;
            } else {
                //已登录,根据用户名查找购物车列表
                List<Cart> redisCartList = cartService.findCartListByUsername(username);
                if (cookieCartList != null && cookieCartList.size() > 0) {
                    //1. 将cookie中的购物车与redis中的购物车列表合并到一个新列表
                    redisCartList = cartService.mergeCartList(cookieCartList, redisCartList);
                    cartService.saveCartListInRedisByUsername(redisCartList,username);
                    //2.删除cookie中的购物车
                    CookieUtils.deleteCookie(request, response, COOKIE_CART_LIST);
                }
                return redisCartList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //购物车增减商品
    //http://cart.pinyougou.com/cart/addItemToCartList.do?itemId=xxx&num=x

    /**
     * 根据商品 id 查询商品和购买数量加入到 cartList
     *
     * @param itemId 商品sku id
     * @param num    购买商品数据
     * @return
     */
    @GetMapping("/addItemToCartList")
    public Result addItemToCartList(Long itemId, Integer num) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Cart> cartList = findCartList();
            //将商品加入购物车列表
            List<Cart> newCartList = cartService.addItemToCartList(cartList, itemId, num);
            //匿名登录 将商品写到cookie
            if ("anonymousUser".equals(username)) {
                String cartListJsonStr = JSON.toJSONString(newCartList);
                CookieUtils.setCookie(request, response, COOKIE_CART_LIST, cartListJsonStr, COOKIE_CART_MAX_AGE, true);
            } else {//已登录,将商品写到redis
                cartService.saveCartListInRedisByUsername(newCartList, username);
            }
            return Result.ok("加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("加入购物车失败");
    }
}
