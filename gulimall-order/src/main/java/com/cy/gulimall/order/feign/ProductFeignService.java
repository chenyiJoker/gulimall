package com.cy.gulimall.order.feign;

import com.cy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/skuId/{id}skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id") Long id);

}
