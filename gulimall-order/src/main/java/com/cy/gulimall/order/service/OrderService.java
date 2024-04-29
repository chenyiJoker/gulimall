package com.cy.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.common.to.mq.SeckillOrderTo;
import com.cy.common.utils.PageUtils;
import com.cy.gulimall.order.entity.OrderEntity;
import com.cy.gulimall.order.vo.OrderConfirmVo;
import com.cy.gulimall.order.vo.OrderSubmitVo;
import com.cy.gulimall.order.vo.PayVo;
import com.cy.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 11:51:52
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirm() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo);

    OrderEntity getOrderStatus(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    void createSeckillOrder(SeckillOrderTo orderTo);

    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);
}

