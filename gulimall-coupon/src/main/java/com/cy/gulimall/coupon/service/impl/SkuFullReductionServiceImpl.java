package com.cy.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.to.MemberPrice;
import com.cy.common.to.SkuReductionTo;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.gulimall.coupon.dao.SkuFullReductionDao;
import com.cy.gulimall.coupon.entity.MemberPriceEntity;
import com.cy.gulimall.coupon.entity.SkuFullReductionEntity;
import com.cy.gulimall.coupon.entity.SkuLadderEntity;
import com.cy.gulimall.coupon.service.MemberPriceService;
import com.cy.gulimall.coupon.service.SkuFullReductionService;
import com.cy.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService ladderService;

    @Autowired
    private MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // sms_sku_ladder
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReductionTo, ladderEntity);
        ladderEntity.setAddOther(skuReductionTo.getCountStatus());

        if (skuReductionTo.getFullCount() > 0) {
            ladderService.save(ladderEntity);
        }

        // sms_sku_full_reduction
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, fullReductionEntity);
        fullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());
        if (skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
            this.save(fullReductionEntity);
        }

        // sms_member_price
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setAddOther(1);
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());

            return memberPriceEntity;
        }).filter(item -> item.getMemberPrice().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }

}