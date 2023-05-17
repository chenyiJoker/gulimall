package com.cy.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;

import com.cy.gulimall.ware.dao.PurchaseDetailDao;
import com.cy.gulimall.ware.entity.PurchaseDetailEntity;
import com.cy.gulimall.ware.service.PurchaseDetailService;
import org.springframework.util.StringUtils;
import sun.plugin.javascript.navig4.LayerArray;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        String status = (String) params.get("status");
        String wareId = (String) params.get("wareId");
        String key = (String) params.get("key");

        if (StringUtils.hasText(key)) {
            queryWrapper.and(obj -> {
                obj.eq(PurchaseDetailEntity::getPurchaseId, key)
                        .or().eq(PurchaseDetailEntity::getSkuId, key);
            });
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(PurchaseDetailEntity::getStatus, status);
        }
        if (StringUtils.hasText(wareId)) {
            queryWrapper.eq(PurchaseDetailEntity::getWareId, wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseDetailEntity::getPurchaseId, id);
        return this.list(queryWrapper);
    }
}