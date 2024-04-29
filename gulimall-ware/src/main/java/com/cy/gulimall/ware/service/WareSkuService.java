package com.cy.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.common.to.OrderTo;
import com.cy.common.to.mq.StockLockedTo;
import com.cy.common.utils.PageUtils;
import com.cy.gulimall.ware.entity.WareSkuEntity;
import com.cy.gulimall.ware.vo.SkuHasStockVo;
import com.cy.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 11:59:39
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo stockLockedTo);

    void unlockStock(OrderTo orderTo);
}

