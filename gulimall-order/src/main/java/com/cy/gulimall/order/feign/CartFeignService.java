package com.cy.gulimall.order.feign;

import com.cy.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping(value = "/currentUserCartItems")
    @ResponseBody
    List<OrderItemVo> getCurrentCartItems();
}
