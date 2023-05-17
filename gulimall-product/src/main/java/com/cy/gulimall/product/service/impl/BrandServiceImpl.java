package com.cy.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.gulimall.product.service.CategoryBrandRelationService;
import com.cy.gulimall.product.vo.BrandVo;
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

import com.cy.gulimall.product.dao.BrandDao;
import com.cy.gulimall.product.entity.BrandEntity;
import com.cy.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<BrandEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(key)) {
            queryWrapper.eq(BrandEntity::getBrandId, key).or().like(BrandEntity::getName, key);
        }
        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        // 保证冗余字段的一致
        this.updateById(brand);
        if (StringUtils.hasText(brand.getName())) {
            // 同步更新其他关联表的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());

            // TODO 更新其他关联
        }
    }

    @Override
    public List<BrandVo> getBrandsByIds(List<Long> brandIds) {
        List<BrandEntity> brandEntities = baseMapper.selectList(new LambdaQueryWrapper<BrandEntity>().in(BrandEntity::getBrandId, brandIds));
        return brandEntities.stream().map(brandEntity -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandName(brandEntity.getName());
            brandVo.setBrandId(brandEntity.getBrandId());
            return brandVo;
        }).collect(Collectors.toList());
    }

}