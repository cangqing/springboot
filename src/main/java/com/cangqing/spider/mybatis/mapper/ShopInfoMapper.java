package com.cangqing.spider.mybatis.mapper;

import com.cangqing.spider.mybatis.beans.ShopInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by song on 2017/7/10.
 */
public interface ShopInfoMapper {
    void add(ShopInfo shopInfo);
    ShopInfo findByShopId(String shopId);
    List<ShopInfo> findByShopIdList(List<String> shopIdList);
    void updateByShopId(Map<String, Object> params);
    List<ShopInfo> findAll(Map<String, Object> params);
}
