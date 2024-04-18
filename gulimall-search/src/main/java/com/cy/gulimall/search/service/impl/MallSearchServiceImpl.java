package com.cy.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cy.common.to.es.SkuEsModel;
import com.cy.common.utils.R;
import com.cy.gulimall.search.config.ESConfig;
import com.cy.gulimall.search.constant.EsConstant;
import com.cy.gulimall.search.feign.ProductFeignService;
import com.cy.gulimall.search.service.MallSearchService;
import com.cy.gulimall.search.vo.AttrResponseVo;
import com.cy.gulimall.search.vo.BrandVo;
import com.cy.gulimall.search.vo.SearchParam;
import com.cy.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result;

        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            SearchResponse response = client.search(searchRequest, ESConfig.COMMON_OPTIONS);
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // 构建结果数据
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        // 1.查询到的商品
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.hasText(param.getKeyword())) {
                    String skuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].string();
                    esModel.setSkuTitle(skuTitle);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        // 2.属性
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 1.属性id
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            // 2.属性名字
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            // 3.属性的所有值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");

            List<String> attrValues = attrValueAgg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());

            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 3.品牌
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();

        ParsedLongTerms brandsAgg = response.getAggregations().get("brands_agg");
        for (Terms.Bucket bucket : brandsAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 1.品牌id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            // 2.品牌名字
            ParsedStringTerms brandsNameAgg = bucket.getAggregations().get("brands_name_agg");
            brandVo.setBrandName(brandsNameAgg.getBuckets().get(0).getKeyAsString());
            // 3.品牌图片
            ParsedStringTerms brandsImgAgg = bucket.getAggregations().get("brands_img_agg");
            brandVo.setBrandImg(brandsImgAgg.getBuckets().get(0).getKeyAsString());
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 4.分类
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 1.分类id
            catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
            // 2.分类名字
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            catalogVo.setCatalogName(catalogNameAgg.getBuckets().get(0).getKeyAsString());
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // 5.分页-当前页码
        result.setPageNum(param.getPageNum());
        // 5.分页-总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // 5.分页-总页码
        int totalPages = (int) (total - 1) / EsConstant.PRODUCT_PAGESIZE + 1;
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 6.构建面包屑导航功能
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);

                result.getAttrIds().add(Long.parseLong(s[0]));
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("品牌");
            R info = productFeignService.brandsInfo(param.getBrandId());
            if (info.getCode() == 0) {
                List<BrandVo> brand = info.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName()).append(";");
                    replace = replaceQueryString(param, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }

        return result;
    }

    private static String replaceQueryString(SearchParam param, String value, String key) {
        String encode;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }

    // 准备检索请求
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder searchSource = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 模糊查询
        if (StringUtils.hasText(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 三级分类id
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        // 品牌id
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        // 所有指定的属性
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nesteBoolQuery = QueryBuilders.boolQuery();

                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValue = s[1].split(":");
                nesteBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nesteBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));

                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nesteBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }

        // 是否有货
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        // 价格区间
        if (StringUtils.hasText(param.getSkuPrice())) {
            // 1_500 500_ _500
            String[] s = param.getSkuPrice().split("_");

            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            if (s.length == 2) {
                if (s[0] != null) {
                    rangeQuery.gte(s[0]);
                }
                rangeQuery.lte(s[1]);
            } else if (s.length == 1) {
                rangeQuery.gte(s[0]);
            }

            boolQuery.filter(rangeQuery);
        }

        searchSource.query(boolQuery);

        // 排序
        if (StringUtils.hasText(param.getSort())) {
            String[] s = param.getSort().split("_");
            SortOrder sort = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSource.sort(s[0], sort);
        }

        // 分页
        searchSource.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSource.size(EsConstant.PRODUCT_PAGESIZE);

        // 高亮
        if (StringUtils.hasText(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();

            builder.field("skuTitle");
            builder.preTags("<b style = 'color:red'>");
            builder.postTags("</b>");
            searchSource.highlighter(builder);
        }

        // 聚合分析
        // 1.品牌聚合
        TermsAggregationBuilder brandsAgg = AggregationBuilders.terms("brands_agg");
        brandsAgg.field("brandId").size(50);
        // 品牌聚合的子聚合
        brandsAgg.subAggregation(AggregationBuilders.terms("brands_name_agg").field("brandName").size(1));
        brandsAgg.subAggregation(AggregationBuilders.terms("brands_img_agg").field("brandImg").size(1));

        searchSource.aggregation(brandsAgg);

        // 2.分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSource.aggregation(catalogAgg);

        // 3.属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(20);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        attrAgg.subAggregation(attrIdAgg);
        searchSource.aggregation(attrAgg);

        System.out.println(searchSource);

        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSource);
    }
}
