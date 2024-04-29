package com.cy.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.constant.ProductConstant;
import com.cy.common.to.SkuHasStockVo;
import com.cy.common.to.SkuReductionTo;
import com.cy.common.to.SpuBoundsTo;
import com.cy.common.to.es.SkuEsModel;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.common.utils.R;
import com.cy.gulimall.product.dao.SpuInfoDao;
import com.cy.gulimall.product.entity.*;
import com.cy.gulimall.product.feign.CouponFeignServcie;
import com.cy.gulimall.product.feign.SearchFeignService;
import com.cy.gulimall.product.feign.WareFeignService;
import com.cy.gulimall.product.service.*;
import com.cy.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService descService;
    @Autowired
    private SpuImagesService imagesService;

    @Autowired
    private ProductAttrValueService valueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;

    @Autowired
    private CouponFeignServcie couponFeignServcie;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            queryWrapper.and(obj -> obj.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key));
        }
        String status = (String) params.get("status");
        queryWrapper.eq(StringUtils.hasText(status), SpuInfoEntity::getPublishStatus, status);

        String catelogId = (String) params.get("catelogId");
        queryWrapper.eq(StringUtils.hasText(catelogId), SpuInfoEntity::getCatalogId, catelogId);

        String brandId = (String) params.get("brandId");
        queryWrapper.eq(StringUtils.hasText(brandId), SpuInfoEntity::getBrandId, brandId);


        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1,保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        // 2,保存spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        descService.save(spuInfoDescEntity);

        // 3,保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(spuInfoEntity.getId(), images);

        // 4,保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        valueService.saveBaseAttrs(spuInfoEntity.getId(), baseAttrs);


        // 5,保存spu的积分信息 gulimall_sms->sms_spu_bounds
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(vo.getBounds(), spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        couponFeignServcie.saveSpuBounds(spuBoundsTo);

        // 6,保存当前spu对应的所有sku的信息
        List<Skus> skus = vo.getSkus();

        if (!skus.isEmpty()) {
            skus.forEach(sku -> {
                String defaultImg = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                // 6.1 sku的基本信息 pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImg);

                skuInfoService.saveSkuInfo(skuInfoEntity);

                // 6.2 sku的图片信息 pms_sku_images
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> collect = sku.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(item -> !StringUtils.isEmpty(item.getImgUrl())).collect(Collectors.toList());

                skuImagesService.saveBatch(collect);

                // 6.3 sku的销售属性 pms_sku_sale_attr_value
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = attr.stream().map(item -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(item, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());

                saleAttrValueService.saveBatch(saleAttrValueEntities);

                // 6.4 sku的优惠,满减等信息 gulimall_sms ->sms_sku_ladder sms_sku_full_reduction sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
                    couponFeignServcie.saveSkuReduction(skuReductionTo);
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
    }

    @Override
    public void spuUp(Long spuId) {
        // 1.查出当前spuid对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        // 查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> listforspu = valueService.listforspu(spuId);

        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        List<Long> attrIds = listforspu.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);

        Set<Long> idSet = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrsList = listforspu.stream().filter(attr -> idSet.contains(attr.getAttrId())).map(attr -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(attr, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        // 远程调用
        Map<Long, Boolean> stockMap = null;
        try {
            R skuHasStock = wareFeignService.getSkuHasStock(skuIds);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {
            };
            stockMap = skuHasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));

        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
        }

        // 2.封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            // 组装需要的数据
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);

            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            BrandEntity brand = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());

            CategoryEntity category = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(category.getName());

            // 设置库存信息
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            skuEsModel.setHotScore(0L);

            // 设置检索属性
            skuEsModel.setAttrs(attrsList);
            return skuEsModel;
        }).collect(Collectors.toList());

        // 将数据发给es进行保存
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {
            // 远程调用成功，修改spu状态
            SpuInfoEntity spuInfoEntity = baseMapper.selectById(spuId);
            spuInfoEntity.setPublishStatus(ProductConstant.StatusEnum.SPU_UP.getCode());
            baseMapper.updateById(spuInfoEntity);
        } else {
            // TODO 重复调用
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long id) {
        SkuInfoEntity skuInfo = skuInfoService.getById(id);
        return this.getById(skuInfo.getSpuId());
    }
}