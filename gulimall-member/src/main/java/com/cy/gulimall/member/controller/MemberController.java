package com.cy.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.cy.common.exception.BizCodeEnume;
import com.cy.gulimall.member.exception.PhoneExistException;
import com.cy.gulimall.member.exception.UserNameExistException;
import com.cy.gulimall.member.feign.CouponFeignService;
import com.cy.gulimall.member.vo.MemberLoginVo;
import com.cy.gulimall.member.vo.MemberRegisterVo;
import com.cy.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cy.gulimall.member.entity.MemberEntity;
import com.cy.gulimall.member.service.MemberService;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.R;



/**
 * 会员
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 11:45:38
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser user) throws Exception {
        MemberEntity memberEntity = memberService.oauthLogin(user);
        if (memberEntity == null) {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMessage());
        }
        return R.ok().setData(memberEntity);
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity == null) {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMessage());
        }
        return R.ok().setData(memberEntity);
    }

    @PostMapping("/regist")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            memberService.register(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnume.USERNAME_EXIST_EXCEPTION.getCode(), BizCodeEnume.USERNAME_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }


    @RequestMapping("coupon")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("陈毅");
        R membercoupon = couponFeignService.membercoupon();
        return R.ok().put("member", memberEntity).put("coupon", membercoupon.get("coupon"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
