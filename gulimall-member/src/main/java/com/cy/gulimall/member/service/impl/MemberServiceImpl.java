package com.cy.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.HttpUtils;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;
import com.cy.gulimall.member.dao.MemberDao;
import com.cy.gulimall.member.entity.MemberEntity;
import com.cy.gulimall.member.entity.MemberLevelEntity;
import com.cy.gulimall.member.exception.PhoneExistException;
import com.cy.gulimall.member.exception.UserNameExistException;
import com.cy.gulimall.member.service.MemberLevelService;
import com.cy.gulimall.member.service.MemberService;
import com.cy.gulimall.member.vo.MemberLoginVo;
import com.cy.gulimall.member.vo.MemberRegisterVo;
import com.cy.gulimall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {
        MemberEntity memberEntity = new MemberEntity();

        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getUserName());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(passwordEncoder.encode(vo.getPassword()));

        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        if (baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, phone)) > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        if (baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, userName)) > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        MemberEntity memberEntity = baseMapper.selectOne(new LambdaQueryWrapper<MemberEntity>()
                .eq(MemberEntity::getUsername, vo.getLoginacct())
                .or().eq(MemberEntity::getMobile, vo.getLoginacct()));
        if (memberEntity == null) {
            return null;
        } else {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(vo.getPassword(), memberEntity.getPassword());
            if (matches) {
                return memberEntity;
            }
        }
        return null;
    }

    @Override
    public MemberEntity oauthLogin(SocialUser user) throws Exception {
        String uid = user.getUid();
        MemberEntity memberEntity = baseMapper.selectOne(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getSocialUid, uid));
        if (memberEntity != null) {
            memberEntity.setExpiresIn(user.getExpires_in());
            memberEntity.setAccessToken(user.getAccess_token());

            baseMapper.updateById(memberEntity);
            return memberEntity;
        } else {
            MemberEntity member = new MemberEntity();
            try {
                Map<String, String> map = new HashMap<>();
                map.put("access_token", user.getAccess_token());
                HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), map);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);

                    member.setNickname(jsonObject.getString("name"));
                    member.setCreateTime(jsonObject.getDate("created_at"));
                    member.setEmail(jsonObject.getString("email"));
                    member.setHeader(jsonObject.getString("avatar_url"));

                    MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
                    member.setLevelId(levelEntity.getId());
                }
            } catch (Exception ignored) {

            }
            member.setSocialUid(user.getUid());
            member.setAccessToken(user.getAccess_token());
            member.setExpiresIn(user.getExpires_in());
            baseMapper.insert(member);

            return member;
        }
    }

}