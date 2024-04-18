package com.cy.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.gulimall.product.dao.ProductAttrValueDao;
import com.cy.gulimall.product.entity.AttrEntity;
import com.cy.gulimall.product.entity.ProductAttrValueEntity;
import com.cy.gulimall.product.service.AttrService;
import com.cy.gulimall.product.service.ProductAttrValueService;
import com.cy.gulimall.product.vo.BaseAttrs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveBaseAttrs(Long id, List<BaseAttrs> baseAttrs) {
        if (!baseAttrs.isEmpty()) {
            List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
                ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
                valueEntity.setAttrId(attr.getAttrId());
                AttrEntity attrEntity = attrService.getById(attr.getAttrId());
                valueEntity.setAttrName(attrEntity.getAttrName());
                valueEntity.setAttrValue(attr.getAttrValues());
                valueEntity.setQuickShow(attr.getShowDesc());
                valueEntity.setSpuId(id);
                return valueEntity;
            }).collect(Collectors.toList());
            this.saveBatch(collect);
        }
    }

    @Override
    public List<ProductAttrValueEntity> listforspu(Long spuId) {
        LambdaQueryWrapper<ProductAttrValueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductAttrValueEntity::getSpuId, spuId);
        return baseMapper.selectList(queryWrapper);
    }

    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        LambdaQueryWrapper<ProductAttrValueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductAttrValueEntity::getSpuId, spuId);
        baseMapper.delete(queryWrapper);
        List<ProductAttrValueEntity> collect = entities.stream().peek(item -> item.setSpuId(spuId)).collect(Collectors.toList());
        this.saveBatch(collect);
    }

}