package com.cy.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.cy.common.to.es.SkuEsModel;
import com.cy.gulimall.search.config.ESConfig;
import com.cy.gulimall.search.constant.EsConstant;
import com.cy.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            // 构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            indexRequest.source(JSON.toJSONString(skuEsModel), XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ESConfig.COMMON_OPTIONS);

        //TODO 如果批量错误
        List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
        log.info("商品上架成果{}", collect);

        return bulk.hasFailures();
    }
}
