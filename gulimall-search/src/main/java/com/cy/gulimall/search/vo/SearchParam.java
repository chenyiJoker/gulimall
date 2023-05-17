package com.cy.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    /**
     * 完整查询参数
     * keyword=小米
     * &sort=saleCount_desc/asc&hasStock=0/1&skuPrice=400_1900&brandId=1&catalog3Id=1&at
     * trs=1_3G:4G:5G&attrs=2_骁龙845&attrs=4_高清屏
     */
    private String keyword;
    private String sort;
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private Long catalog3Id;
    private List<String> attrs;
    private Integer pageNum = 1;
    private String _queryString;

}
