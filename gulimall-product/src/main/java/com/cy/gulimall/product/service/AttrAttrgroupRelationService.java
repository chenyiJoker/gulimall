package com.cy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.common.utils.PageUtils;
import com.cy.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.cy.gulimall.product.vo.AttrGounpRelationVo;
import com.cy.gulimall.product.vo.AttrGounpWithAttrsVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 01:03:02
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatch(List<AttrGounpRelationVo> vos);

}

