package com.cy.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.common.utils.PageUtils;
import com.cy.gulimall.member.entity.MemberEntity;
import com.cy.gulimall.member.exception.PhoneExistException;
import com.cy.gulimall.member.exception.UserNameExistException;
import com.cy.gulimall.member.vo.MemberLoginVo;
import com.cy.gulimall.member.vo.MemberRegisterVo;
import com.cy.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 11:45:38
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUserNameUnique(String userName) throws UserNameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity oauthLogin(SocialUser user) throws Exception;
}

