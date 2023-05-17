package com.cy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.common.utils.PageUtils;
import com.cy.gulimall.product.entity.SkuInfoEntity;
import com.cy.gulimall.product.vo.SkuItemVo;
import com.cy.gulimall.product.vo.Skus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 01:03:02
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo skuItem(Long skuId) throws ExecutionException, InterruptedException;
}

