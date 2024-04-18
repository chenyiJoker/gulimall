package com.cy.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.gulimall.product.dao.AttrGroupDao;
import com.cy.gulimall.product.entity.AttrEntity;
import com.cy.gulimall.product.entity.AttrGroupEntity;
import com.cy.gulimall.product.service.AttrGroupService;
import com.cy.gulimall.product.service.AttrService;
import com.cy.gulimall.product.vo.AttrGounpWithAttrsVo;
import com.cy.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrGroupEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(key)) {
            queryWrapper.and((obj) -> obj.eq(AttrGroupEntity::getAttrGroupId, key).or().like(AttrGroupEntity::getAttrGroupName, key));
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);

            return new PageUtils(page);
        } else {
            queryWrapper.eq(AttrGroupEntity::getCatelogId, catelogId);

            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGounpWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new LambdaQueryWrapper<AttrGroupEntity>()
                .eq(AttrGroupEntity::getCatelogId, catelogId));
        List<AttrGounpWithAttrsVo> collect = attrGroupEntities.stream().map((group) -> {
            AttrGounpWithAttrsVo attrGounpWithAttrsVo = new AttrGounpWithAttrsVo();
            BeanUtils.copyProperties(group, attrGounpWithAttrsVo);

            List<AttrEntity> attrs = attrService.getRelationAttr(attrGounpWithAttrsVo.getAttrGroupId());
            if (attrs != null) {
                attrGounpWithAttrsVo.setAttrs(attrs);
            }

            return attrGounpWithAttrsVo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }
}