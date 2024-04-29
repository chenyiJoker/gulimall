package com.cy.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.gulimall.coupon.dao.SeckillSessionDao;
import com.cy.gulimall.coupon.entity.SeckillSessionEntity;
import com.cy.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.cy.gulimall.coupon.service.SeckillSessionService;
import com.cy.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {

        //计算最近三天
        //查出这三天参与秒杀活动的商品
        List<SeckillSessionEntity> list = this.lambdaQuery().between(SeckillSessionEntity::getStartTime, startTime(), endTime()).list();

        if (list != null && !list.isEmpty()) {
            List<SeckillSessionEntity> collect = list.stream().peek(session -> {
                //查出sms_seckill_sku_relation表中关联的skuId
                List<SeckillSkuRelationEntity> relationSkus = seckillSkuRelationService.lambdaQuery()
                        .eq(SeckillSkuRelationEntity::getPromotionSessionId, session.getId())
                        .list();
                session.setRelationSkus(relationSkus);
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    /**
     * 当前时间
     *
     * @return
     */
    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);

        //格式化时间
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 结束时间
     *
     * @return
     */
    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(plus, max);

        //格式化时间
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}