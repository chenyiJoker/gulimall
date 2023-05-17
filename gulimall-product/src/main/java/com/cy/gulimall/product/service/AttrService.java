package com.cy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.common.utils.PageUtils;
import com.cy.gulimall.product.entity.AttrEntity;
import com.cy.gulimall.product.vo.AttrGounpRelationVo;
import com.cy.gulimall.product.vo.AttrRespVo;
import com.cy.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 01:03:02
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryAttrBasePage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrGounpId);

    void deleteRelation(AttrGounpRelationVo[] relationVo);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGounpId);

    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

