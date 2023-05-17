package com.cy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.common.utils.PageUtils;
import com.cy.gulimall.product.entity.BrandEntity;
import com.cy.gulimall.product.vo.BrandVo;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 01:03:02
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);

    List<BrandVo> getBrandsByIds(List<Long> brandIds);
}

