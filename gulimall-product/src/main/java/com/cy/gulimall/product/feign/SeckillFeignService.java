package com.cy.gulimall.product.feign;

import com.cy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-seckill")
public interface SeckillFeignService {
    @GetMapping(value = "/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId")Long skuId);
}
