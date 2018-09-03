package com.cangqing.spider.mybatis.mapper;

import com.cangqing.spider.mybatis.beans.BrandInfo;

import java.util.List;

/**
 * Created by song on 2017/7/10.
 */
public interface BrandInfoMapper {
    void add(BrandInfo brandInfo);
    List<BrandInfo> findAllByFlag(Integer flag);
    BrandInfo findById(String name);
    BrandInfo findByName(String name);
    void setFlagByName(String name);
}
