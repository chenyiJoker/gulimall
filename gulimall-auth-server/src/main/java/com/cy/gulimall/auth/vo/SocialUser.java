package com.cy.gulimall.auth.vo;

import lombok.Data;

@Data
public class SocialUser {
    private String access_token;

    private String remind_in;
    private String refresh_token;

    private String uid;

}
