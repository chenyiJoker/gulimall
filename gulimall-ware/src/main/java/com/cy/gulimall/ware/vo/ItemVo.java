package com.cy.gulimall.ware.vo;

import lombok.Data;

@Data
public class ItemVo {
//    items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
    private Long itemId;
    private Integer status;
    private String reason;
}
