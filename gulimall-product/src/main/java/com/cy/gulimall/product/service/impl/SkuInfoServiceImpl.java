package com.cy.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.common.utils.R;
import com.cy.gulimall.product.dao.SkuInfoDao;
import com.cy.gulimall.product.entity.SkuImagesEntity;
import com.cy.gulimall.product.entity.SkuInfoEntity;
import com.cy.gulimall.product.entity.SpuInfoDescEntity;
import com.cy.gulimall.product.feign.SeckillFeignService;
import com.cy.gulimall.product.service.*;
import com.cy.gulimall.product.vo.SeckillSkuVo;
import com.cy.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            queryWrapper.and(obj -> obj.eq(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key));
        }

        String catelogId = (String) params.get("catelogId");
        queryWrapper.eq(StringUtils.hasText(catelogId), SkuInfoEntity::getCatalogId, catelogId);

        String brandId = (String) params.get("brandId");
        queryWrapper.eq(StringUtils.hasText(brandId), SkuInfoEntity::getBrandId, brandId);

        String min = (String) params.get("min");
        queryWrapper.ge(StringUtils.hasText(min), SkuInfoEntity::getPrice, min);

        String max = (String) params.get("max");
        if (StringUtils.hasText(max)) {
            BigDecimal bigDecimal = new BigDecimal(max);
            if (bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                queryWrapper.le(SkuInfoEntity::getPrice, max);
            }
        }
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuInfoEntity::getSpuId, spuId);
        return this.list(queryWrapper);
    }

    @Override
    public SkuItemVo skuItem(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1、sku基本信息的获取  pms_sku_info
            SkuInfoEntity info = this.getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 3、获取spu的销售属性组合
            List<SkuItemVo.SkuItemSaleAttrVo> saleAttr = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttr);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4、获取spu的介绍    pms_spu_info_desc
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> groupAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 5、获取spu的规格参数信息
            List<SkuItemVo.SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2、sku的图片信息    pms_sku_images
            List<SkuImagesEntity> imagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(imagesEntities);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            //3、远程调用查询当前sku是否参与秒杀优惠活动
            R skuSeckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
            if (skuSeckillInfo.getCode() == 0) {
                //查询成功
                SeckillSkuVo seckillInfoData = skuSeckillInfo.getData(new TypeReference<SeckillSkuVo>() {
                });
                skuItemVo.setSeckillSkuVo(seckillInfoData);

                if (seckillInfoData != null) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime > seckillInfoData.getEndTime()) {
                        skuItemVo.setSeckillSkuVo(null);
                    }
                }
            }
        }, executor);


        CompletableFuture.allOf(saleAttrFuture, descFuture, groupAttrFuture, imageFuture, seckillFuture).get();

        return skuItemVo;
    }
}