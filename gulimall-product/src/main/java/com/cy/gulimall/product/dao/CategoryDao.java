package com.cy.gulimall.product.dao;

import com.cy.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 01:03:02
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
