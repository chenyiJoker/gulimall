package com.cy.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.common.utils.PageUtils;
import com.cy.gulimall.ware.entity.WareInfoEntity;
import com.cy.gulimall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 11:59:39
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long addrId);
}

