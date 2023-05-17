package com.cy.gulimall.search.controller;

import com.cy.common.exception.BizCodeEnume;
import com.cy.common.to.es.SkuEsModel;
import com.cy.common.utils.R;
import com.cy.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("/search/save")
@RestController
@Slf4j
public class ElasticSaveController {
    @Autowired
    private ProductSaveService productSaveService;
    boolean flag = false;
    // 上架商品
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        try {
            flag = productSaveService.productStatusUp(skuEsModels);
        } catch (Exception e) {
            log.error("商品上架错误{}", e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (flag) {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMessage());
        } else {
            return R.ok();
        }
    }
}
