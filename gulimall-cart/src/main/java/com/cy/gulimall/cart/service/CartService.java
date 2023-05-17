package com.cy.gulimall.cart.service;

import com.cy.gulimall.cart.vo.Cart;
import com.cy.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    void addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    List<CartItem> getUserCartItems();

    void clearCartInfo(String cartKey);

    void checkItem(Long skuId, Integer checked);

    void changeItemCount(Long skuId, Integer num);

    void deleteIdCartInfo(Integer skuId);
}
