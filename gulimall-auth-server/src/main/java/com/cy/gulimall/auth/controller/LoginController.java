package com.cy.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.cy.common.constant.AuthServerConstant;
import com.cy.common.exception.BizCodeEnume;
import com.cy.common.utils.R;
import com.cy.common.vo.MemberResponseVo;
import com.cy.gulimall.auth.fegin.MemberFeginServcie;
import com.cy.gulimall.auth.fegin.ThirdPartyFeginService;
import com.cy.gulimall.auth.vo.UserLoginVo;
import com.cy.gulimall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Controller
public class LoginController {

    @Autowired
    private ThirdPartyFeginService thirdPartyFeginService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MemberFeginServcie memberFeginServcie;

    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);

        if (StringUtils.hasText(redisCode) && System.currentTimeMillis() - Long.parseLong(redisCode.split("_")[1]) < 60000) {
            return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMessage());
        }

        int code = (int) ((Math.random() * 9 + 1) * 100000);
        String codeNum = String.valueOf(code);

        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, codeNum + "_" + System.currentTimeMillis(), 10, TimeUnit.MINUTES);
        thirdPartyFeginService.sendSmsCode(phone, codeNum);
        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            //效验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        // 校验验证码
        String code = vo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.hasText(redisCode)) {
            if (code.equals(redisCode.split("_")[0])) {
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

                R r = memberFeginServcie.register(vo);
                if (r.getCode() == 0) {
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    //效验出错回到注册页面
                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                //效验出错回到注册页面
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            //效验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        R r = memberFeginServcie.login(vo);
        if (r.getCode() == 0) {
            MemberResponseVo data = r.getData(new TypeReference<MemberResponseVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loinPage(HttpSession session) {
        // 从session先取出来用户的信息，判断用户是否已经登录过了
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        // 如果用户没登录那就跳转到登录页面
        if (attribute == null) {
            return "login";
        } else {
            return "redirect:http://gulimall.com";
        }
    }

    @GetMapping(value = "/logout.html")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute(AuthServerConstant.LOGIN_USER);
        request.getSession().invalidate();
        return "redirect:http://gulimall.com";
    }
}
