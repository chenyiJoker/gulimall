package com.cy.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.common.utils.R;
import com.cy.gulimall.ware.dao.WareInfoDao;
import com.cy.gulimall.ware.entity.WareInfoEntity;
import com.cy.gulimall.ware.feign.MemberFeignService;
import com.cy.gulimall.ware.service.WareInfoService;
import com.cy.gulimall.ware.vo.FareVo;
import com.cy.gulimall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<WareInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(key)) {
            queryWrapper.eq(WareInfoEntity::getId, key)
                    .or().like(WareInfoEntity::getName, key)
                    .or().like(WareInfoEntity::getAddress, key)
                    .or().like(WareInfoEntity::getAreacode, key);
        }
        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        R r = memberFeignService.addressInfo(addrId);
        if (r == null) {
            return null;
        }
        MemberAddressVo data = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        if (data == null) {
            return null;
        }
        BigDecimal fare = new BigDecimal(Integer.parseInt(data.getPhone()) % 10);
        FareVo fareVo = new FareVo();
        fareVo.setAddress(data);
        fareVo.setFare(fare);
        return fareVo;
    }

}