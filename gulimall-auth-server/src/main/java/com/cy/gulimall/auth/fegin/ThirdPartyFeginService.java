package com.cy.gulimall.auth.fegin;

import com.cy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")
public interface ThirdPartyFeginService {
    @GetMapping("/sms/sendCode")
    public R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
