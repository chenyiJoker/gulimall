package com.cy.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.common.utils.PageUtils;
import com.cy.gulimall.ware.entity.PurchaseEntity;
import com.cy.gulimall.ware.vo.MergeVo;
import com.cy.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 11:59:39
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void merge(MergeVo mergeVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo vo);
}

