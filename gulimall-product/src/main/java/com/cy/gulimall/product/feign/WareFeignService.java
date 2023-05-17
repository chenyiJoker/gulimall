package com.cy.gulimall.product.feign;

import com.cy.common.to.SkuHasStockVo;
import com.cy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds);
}

