package com.cy.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.common.constant.WareConstant;
import com.cy.gulimall.ware.entity.PurchaseDetailEntity;
import com.cy.gulimall.ware.service.PurchaseDetailService;
import com.cy.gulimall.ware.service.WareSkuService;
import com.cy.gulimall.ware.vo.ItemVo;
import com.cy.gulimall.ware.vo.MergeVo;
import com.cy.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;

import com.cy.gulimall.ware.dao.PurchaseDao;
import com.cy.gulimall.ware.entity.PurchaseEntity;
import com.cy.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService detailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new LambdaQueryWrapper<PurchaseEntity>()
                        .eq(PurchaseEntity::getStatus, 0)
                        .or().eq(PurchaseEntity::getStatus, 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void merge(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();

        }
        Long finalPurchaseId = purchaseId;
        List<Long> items = mergeVo.getItems();
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setId(item);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        detailService.updateBatchById(collect);
    }

    @Override
    public void received(List<Long> ids) {
        // 确认采购单状态，并修改
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                    || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            return item;
        }).collect(Collectors.toList());
        this.updateBatchById(collect);

        // 修改采购项的状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> lsit = detailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> entities = lsit.stream().map(i -> {
                PurchaseDetailEntity entity = new PurchaseDetailEntity();
                entity.setId(i.getId());
                entity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return entity;
            }).collect(Collectors.toList());
            detailService.updateBatchById(entities);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo vo) {
        // 修改采购项的状态
        Long id = vo.getId();
        List<ItemVo> items = vo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        boolean flag = true;
        for (ItemVo item : items) {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
            } else {
                // 将成功采购的进行入库
                PurchaseDetailEntity detailEntity = detailService.getById(item.getItemId());

                wareSkuService.addStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
            }
            entity.setStatus(item.getStatus());
            entity.setId(item.getItemId());
            updates.add(entity);
        }
        detailService.updateBatchById(updates);

        // 修改采购单的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);
    }
}