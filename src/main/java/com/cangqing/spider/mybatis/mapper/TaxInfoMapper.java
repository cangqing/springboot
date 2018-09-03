package com.cangqing.spider.mybatis.mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by song on 2017/9/22.
 */
public interface TaxInfoMapper {
    List<Map<String,Object>> find(Map<String, Object> params);
    Integer count(Map<String, Object> params);
}
