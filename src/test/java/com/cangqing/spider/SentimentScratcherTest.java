package com.cangqing.spider;

import com.cangqing.spider.mybatis.beans.MallBrandQuery;
import com.cangqing.spider.mybatis.mapper.MallBrandQueryMapper;
import com.cangqing.spider.scracher.SentimentScratcher;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Created by song on 2017/10/24.
 */
public class SentimentScratcherTest extends GagSpiderApplicationTests {
    @Autowired
    SentimentScratcher sentimentScratcher;
    @Autowired
    MallBrandQueryMapper mallBrandQueryMapper;

    @Test
    public void testScratchSentiment() {
        sentimentScratcher.scratch();
    }

    @Test
    public void testMallBrandQuery() {
        Map<String, Object> params = Maps.newHashMap();
//        params.put("mallId", "19TJRVRADR4KUL2IAG0765M9UE5TCHGM");
        params.put("offset", 0);
        params.put("size", 1000);
        List<MallBrandQuery> mallBrandQueryList = mallBrandQueryMapper.find(params);
        assert (mallBrandQueryList.size()>0);
    }
}
