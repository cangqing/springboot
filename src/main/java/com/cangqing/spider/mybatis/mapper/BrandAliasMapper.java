package com.cangqing.spider.mybatis.mapper;

import com.cangqing.spider.mybatis.beans.BrandAlias;

import java.util.List;

/**
 * Created by song on 2017/9/25.
 */
public interface BrandAliasMapper {
    List<BrandAlias> findByBrandId(Integer brandId);
}
