package com.cangqing.spider.mybatis.mapper;

import com.cangqing.spider.mybatis.beans.EnterpriseInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by song on 2017/7/10.
 */
public interface EnterpriseMapper {
    List<EnterpriseInfo> find(Map<String, Object> params);
    void add(EnterpriseInfo enterpriseInfo);
    void update(EnterpriseInfo enterpriseInfo);
    void updateFlag(String id);
}
