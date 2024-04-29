package com.cy.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.exception.NoStockException;
import com.cy.common.to.OrderTo;
import com.cy.common.to.mq.StockLockedTo;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.common.utils.R;
import com.cy.gulimall.ware.dao.WareSkuDao;
import com.cy.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.cy.gulimall.ware.entity.WareOrderTaskEntity;
import com.cy.gulimall.ware.entity.WareSkuEntity;
import com.cy.gulimall.ware.feign.OrderFeignService;
import com.cy.gulimall.ware.feign.ProductFeignService;
import com.cy.gulimall.ware.service.WareOrderTaskDetailService;
import com.cy.gulimall.ware.service.WareOrderTaskService;
import com.cy.gulimall.ware.service.WareSkuService;
import com.cy.gulimall.ware.vo.OrderItemVo;
import com.cy.gulimall.ware.vo.OrderVo;
import com.cy.gulimall.ware.vo.SkuHasStockVo;
import com.cy.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");

        if (StringUtils.hasText(skuId)) {
            queryWrapper.eq(WareSkuEntity::getSkuId, skuId);
        }
        if (StringUtils.hasText(wareId)) {
            queryWrapper.eq(WareSkuEntity::getWareId, wareId);
        }
        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断是否有这个库存记录
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WareSkuEntity::getSkuId, skuId);
        queryWrapper.eq(WareSkuEntity::getWareId, wareId);
        List<WareSkuEntity> entities = baseMapper.selectList(queryWrapper);
        if (!entities.isEmpty()) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> map = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) map.get("skuName"));
                }
            } catch (Exception ignored) {

            }
            baseMapper.insert(wareSkuEntity);
        } else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            // 查询当前sku的总库存量
            Long count = baseMapper.getSkuStock(skuId);
            SkuHasStockVo vo = new SkuHasStockVo();
            vo.setSkuId(skuId);
            vo.setHasStock(count != null && count > 0);

            return vo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        // 保存库存工作单详情信息  追溯
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(wareOrderTaskEntity);

        //1.找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> stocks = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());
            List<Long> wareIds = baseMapper.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        //2、锁定库存
        for (SkuWareHasStock stock : stocks) {
            boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();
            if (wareIds == null || wareIds.isEmpty()) {
                throw new NoStockException(skuId);
            }

            //1、如果每一个商品都锁定成功,将当前商品锁定了几件的工作单记录发给MQ
            //2、锁定失败。前面保存的工作单信息都回滚了。发送出去的消息，即使要解锁库存，由于在数据库查不到指定的id，所有就不用解锁
            for (Long wareId : wareIds) {
                //锁定成功就返回1，失败就返回0
                Long count = baseMapper.lockSkuStock(skuId, wareId, stock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    WareOrderTaskDetailEntity taskDetailEntity = WareOrderTaskDetailEntity.builder()
                            .skuId(skuId)
                            .skuName("")
                            .skuNum(stock.getNum())
                            .taskId(wareOrderTaskEntity.getId())
                            .wareId(wareId)
                            .lockStatus(1)
                            .build();
                    wareOrderTaskDetailService.save(taskDetailEntity);

                    //TODO 告诉MQ库存锁定成功
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(wareOrderTaskEntity.getId());
                    lockedTo.setDetailId(taskDetailEntity.getId());
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                } else {
                    //当前仓库锁失败，重试下一个仓库
                }
            }

            if (!skuStocked) {
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }

        return null;
    }

    @Override
    public void unlockStock(StockLockedTo stockLockedTo) {
        //库存工作单的id
        Long detailId = stockLockedTo.getId();

        /*
          解锁
          1、查询数据库关于这个订单锁定库存信息
            有：证明库存锁定成功了
               解锁：订单状况
                   1、没有这个订单，必须解锁库存
                   2、有这个订单，不一定解锁库存
                       订单状态：已取消：解锁库存
                               已支付：不能解锁库存
         */
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = wareOrderTaskDetailService.getById(detailId);
        if (wareOrderTaskDetailEntity != null) {
            //查出wms_ware_order_task工作单的信息
            Long id = stockLockedTo.getId();
            WareOrderTaskEntity orderTaskInfo = wareOrderTaskService.getById(id);
            //获取订单号查询订单状态
            String orderSn = orderTaskInfo.getOrderSn();
            //远程查询订单信息
            R orderData = orderFeignService.getOrderStatus(orderSn);
            if (orderData.getCode() == 0) {
                //订单数据返回成功
                OrderVo orderInfo = orderData.getData(new TypeReference<OrderVo>() {
                });

                //判断订单状态是否已取消或者支付或者订单不存在
                if (orderInfo == null || orderInfo.getStatus() == 4) {
                    //订单已被取消，才能解锁库存
                    if (wareOrderTaskDetailEntity.getLockStatus() == 1) {
                        //当前库存工作单详情状态1，已锁定，但是未解锁才可以解锁
                        unLockStock(wareOrderTaskDetailEntity.getSkuId(), wareOrderTaskDetailEntity.getWareId(), wareOrderTaskDetailEntity.getSkuNum(), detailId);
                    }
                }
            } else {
                //消息拒绝以后重新放在队列里面，让别人继续消费解锁
                //远程调用服务失败
                throw new RuntimeException("远程调用服务失败");
            }
        }
    }

    //防止订单服务卡顿，导致订单状态消息一直改不了，库存优先到期，查订单状态新建，什么都不处理
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.lambdaQuery().eq(WareOrderTaskEntity::getOrderSn, orderSn).one();
        Long orderTaskEntityId = orderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.lambdaQuery()
                .eq(WareOrderTaskDetailEntity::getTaskId, orderTaskEntityId)
                .eq(WareOrderTaskDetailEntity::getLockStatus, 1)
                .list();
        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), orderTaskEntityId);
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer skuNum, Long detailId) {
        //库存解锁
        baseMapper.unLockStock(skuId, wareId, skuNum);

        //更新工作单的状态
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(detailId);
        //变为已解锁
        taskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(taskDetailEntity);

    }

    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }
}