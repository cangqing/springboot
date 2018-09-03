package com.cangqing.spider.scracher;

import com.cangqing.spider.http.HttpRequestUtil;
import com.cangqing.spider.mybatis.beans.BrandComment;
import com.cangqing.spider.mybatis.beans.ShopInfo;
import com.cangqing.spider.mybatis.mapper.BrandCommentMapper;
import com.cangqing.spider.mybatis.mapper.ShopInfoMapper;
import com.google.common.collect.Maps;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by song on 2017/8/2.
 */
@Component
public class DianPingCommentScratcher {
    Logger logger = LoggerFactory.getLogger(DianPingCommentScratcher.class);
    @Autowired
    ShopInfoMapper shopInfoMapper;
    @Autowired
    BrandCommentMapper brandCommentMapper;
    ;

    public void scratch() {
        Map params = Maps.newHashMap();
        params.put("offset", 100);
        params.put("size", 10000);
        List<ShopInfo> shopInfoList = shopInfoMapper.findAll(params);
        for (ShopInfo shopInfo : shopInfoList) {
            try {
                String url = "http://www.dianping.com/shop/" + shopInfo.getShopId() + "/review_more_latest";
                logger.info("scratch comments of shopName:" + shopInfo.getBrandName());
                String content = (String) HttpRequestUtil.submitRequestWithHeaders(url, false, RequestHeader.dianpingHeaders);
                Document doc = Jsoup.parse(content);
                Elements commentElements = doc.select(".comment-list>ul>li");
                for (Element element : commentElements) {
                    Elements e = element.getElementsByClass("item-rank-rst");
                    if (e.size() > 0) {
                        String commentLevel = e.first().attr("title");
                        String uid = element.getElementsByClass("J_card").first().attr("user-id");
                        String desc = element.getElementsByClass("J_brief-cont").first().text();
                        String time = element.getElementsByClass("time").first().text();
                        String shopName = element.getElementsByClass("misc-name").first().text();
                        if (time.length() < 6) time = "2017-" + time;
                        BrandComment brandComment = new BrandComment();
                        brandComment.setShopId(shopInfo.getShopId());
                        brandComment.setCommenter(uid);
                        brandComment.setShopName(shopName);
                        brandComment.setCommentDate(time);
                        brandComment.setCommentLevel(commentLevel);
                        brandComment.setCommentContent(desc);
                        brandCommentMapper.add(brandComment);
                    }
                }
            }catch (Exception e){
                logger.error("scratch comment failed:"+shopInfo.getShopId(),e);
            }
        }
    }
}