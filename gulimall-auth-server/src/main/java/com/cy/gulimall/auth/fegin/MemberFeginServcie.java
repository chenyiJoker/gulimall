package com.cy.gulimall.auth.fegin;

import com.cy.common.utils.R;
import com.cy.gulimall.auth.vo.SocialUser;
import com.cy.gulimall.auth.vo.UserLoginVo;
import com.cy.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeginServcie {
    @PostMapping("/member/member/regist")
    public R register(@RequestBody UserRegistVo vo);
    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser user);
}
