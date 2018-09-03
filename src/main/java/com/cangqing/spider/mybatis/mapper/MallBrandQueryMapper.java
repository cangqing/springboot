package com.cangqing.spider.mybatis.mapper;

import com.cangqing.spider.mybatis.beans.MallBrandQuery;

import java.util.List;
import java.util.Map;

/**
 * Created by song on 2017/7/10.
 */
public interface MallBrandQueryMapper {
    void add(MallBrandQuery mallBrandQuery);
    List<MallBrandQuery> find(Map<String, Object> params);
}
