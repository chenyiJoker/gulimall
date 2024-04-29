package com.cy.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cy.common.constant.CartConstant;
import com.cy.common.utils.R;
import com.cy.gulimall.cart.feign.ProductFeignService;
import com.cy.gulimall.cart.interceptor.CartInterceptor;
import com.cy.gulimall.cart.service.CartService;
import com.cy.gulimall.cart.to.UserInfoTo;
import com.cy.gulimall.cart.vo.Cart;
import com.cy.gulimall.cart.vo.CartItem;
import com.cy.gulimall.cart.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public void addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String productRedisValue = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(productRedisValue)) {
            CartItem cartItem = new CartItem();

            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setSkuId(skuId);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setPrice(data.getPrice());
                cartItem.setTitle(data.getSkuTitle());
            }, executor);

            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttrValues(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(getSkuInfo, getSkuSaleAttrValues).get();

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        } else {
            CartItem cartItem = JSON.parseObject(productRedisValue, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String redisValue = (String) cartOps.get(skuId.toString());

        return JSON.parseObject(redisValue, CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            // 1、登录
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            // 临时购物车的键
            String temptCartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();

            // 2、如果临时购物车的数据还未进行合并
            List<CartItem> tempCartItems = getCartItems(temptCartKey);
            if (tempCartItems != null) {
                // 临时购物车有数据需要进行合并操作++
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
                // 清除临时购物车的数据
                clearCartInfo(temptCartKey);
            }

            // 3、获取登录后的购物车数据【包含合并过来的临时购物车的数据和登录后购物车的数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        } else {
            // 没登录
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            // 获取临时购物车里面的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void clearCartInfo(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer checked) {
        // 查询购物车里面的商品
        CartItem cartItem = getCartItem(skuId);
        // 修改商品状态
        cartItem.setCheck(checked == 1);

        // 序列化存入redis中
        String redisValue = JSON.toJSONString(cartItem);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),redisValue);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        // 查询购物车里面的商品
        CartItem cartItem = getCartItem(skuId);
        // 修改商品数量
        cartItem.setCount(num);

        // 序列化存入redis中
        String redisValue = JSON.toJSONString(cartItem);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),redisValue);
    }

    @Override
    public void deleteIdCartInfo(Integer skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && !values.isEmpty()) {
            List<CartItem> collect = values.stream().map((value) -> JSON.parseObject((String) value, CartItem.class)).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo == null) {
            return null;
        } else {
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            return getCartItems(cartKey).stream().filter(CartItem::getCheck)
                    .peek((cartItem) -> cartItem.setPrice(productFeignService.getPrice(cartItem.getSkuId()))).collect(Collectors.toList());
        }
    }

    // 获取到我们要操作的购物车
    private BoundHashOperations<String, Object, Object> getCartOps() {
        // 先得到当前用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        String cartKey;
        if (userInfoTo.getUserId() != null) {
            // gulimall:cart:1
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
        }

        // 绑定指定的key操作Redis
        return redisTemplate.boundHashOps(cartKey);
    }
}
