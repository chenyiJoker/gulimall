package com.cy.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.common.utils.R;
import com.cy.gulimall.ware.feign.ProductFeignService;
import com.cy.gulimall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;

import com.cy.gulimall.ware.dao.WareSkuDao;
import com.cy.gulimall.ware.entity.WareSkuEntity;
import com.cy.gulimall.ware.service.WareSkuService;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");

        if (StringUtils.hasText(skuId)) {
            queryWrapper.eq(WareSkuEntity::getSkuId, skuId);
        }
        if (StringUtils.hasText(wareId)) {
            queryWrapper.eq(WareSkuEntity::getWareId, wareId);
        }
        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断是否有这个库存记录
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WareSkuEntity::getSkuId, skuId);
        queryWrapper.eq(WareSkuEntity::getWareId, wareId);
        List<WareSkuEntity> entities = baseMapper.selectList(queryWrapper);
        if (entities == null || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> map = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) map.get("skuName"));
                }
            } catch (Exception e) {

            }
            baseMapper.insert(wareSkuEntity);
        } else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            // 查询当前sku的总库存量
            Long count = baseMapper.getSkuStock(skuId);
            SkuHasStockVo vo = new SkuHasStockVo();
            vo.setSkuId(skuId);
            vo.setHasStock(count != null && count > 0);

            return vo;
        }).collect(Collectors.toList());

        return collect;
    }
}