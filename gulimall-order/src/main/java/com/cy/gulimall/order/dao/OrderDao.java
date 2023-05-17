package com.cy.gulimall.order.dao;

import com.cy.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 11:51:52
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
