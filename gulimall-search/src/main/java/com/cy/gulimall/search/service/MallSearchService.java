package com.cy.gulimall.search.service;

import com.cy.gulimall.search.vo.SearchParam;
import com.cy.gulimall.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
