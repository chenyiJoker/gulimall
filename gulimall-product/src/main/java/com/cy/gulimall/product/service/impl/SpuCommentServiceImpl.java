package com.cy.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.gulimall.product.dao.SpuCommentDao;
import com.cy.gulimall.product.entity.SpuCommentEntity;
import com.cy.gulimall.product.service.SpuCommentService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("spuCommentService")
public class SpuCommentServiceImpl extends ServiceImpl<SpuCommentDao, SpuCommentEntity> implements SpuCommentService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuCommentEntity> page = this.page(
                new Query<SpuCommentEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

}