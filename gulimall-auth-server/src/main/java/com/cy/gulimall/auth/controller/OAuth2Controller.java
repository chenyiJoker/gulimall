package com.cy.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cy.common.constant.AuthServerConstant;
import com.cy.common.utils.HttpUtils;
import com.cy.common.utils.R;
import com.cy.gulimall.auth.fegin.MemberFeginServcie;
import com.cy.common.vo.MemberResponseVo;
import com.cy.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


@Controller
@Slf4j
public class OAuth2Controller {

    @Autowired
    private MemberFeginServcie memberFeginServcie;

    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "607ab1607cf80a1ac26e2767bfd44ed4ce9ac4ab3024660060189a38521af12d");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/success");
        map.put("grant_type", "authorization_code");
        map.put("client_secret", "7183aeae1716a794fc00fc875e17b81f5f4365ad406d529684a2f35b8dbcde57");
        map.put("code", code);
        // 根据用户授权返回的code换取access_token
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), map, new HashMap<>());

        // 处理
        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            Map<String, String> userMap = new HashMap<>();
            userMap.put("access_token", socialUser.getAccess_token());
            HttpResponse userResponse = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), userMap);
            if (userResponse.getStatusLine().getStatusCode() == 200) {
                String userJson = EntityUtils.toString(userResponse.getEntity());
                JSONObject object = JSON.parseObject(userJson);

                socialUser.setUid(object.getString("id"));

                R oauthLogin = memberFeginServcie.oauthLogin(socialUser);
                if (oauthLogin.getCode() == 0) {
                    MemberResponseVo data = oauthLogin.getData(new TypeReference<MemberResponseVo>() {
                    });
                    session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                    // 登录成功跳回首页
                    return "redirect:http://gulimall.com";
                } else {
                    return "redirect:http://auth.gulimall.com/login.html";
                }
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
