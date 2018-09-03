package com.cangqing.spider.scracher;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cangqing.spider.http.HttpRequestUtil;
import com.cangqing.spider.mybatis.beans.MallInfo;
import com.cangqing.spider.mybatis.beans.ShopInfo;
import com.cangqing.spider.mybatis.mapper.MallInfoMapper;
import com.cangqing.spider.mybatis.mapper.ShopInfoMapper;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by song on 2017/8/2.
 */

@Component
public class DianPingInfoScratcher {
    static Logger logger = LoggerFactory.getLogger(DianPingInfoScratcher.class);

    @Autowired
    MallInfoMapper mallInfoMapper;
    @Autowired
    ShopInfoMapper shopInfoMapper;

    public static Map<String, String> cityMap = new HashMap() {
        {
            put("北京", "2");
            put("天津", "10");
            //put("沈阳", "18");
//            put("大连", "19");
//            put("长春", "70");
//            put("哈尔滨", "79");
//            put("石家庄", "24");
//            put("太原", "35");
//            put("呼和浩特", "46");
//            put("廊坊", "33");
            //华东地区
            put("上海", "1");
            //put("杭州", "3");
            //put("南京", "5");
            //put("苏州", "6");
            //put("无锡", "13");
            put("济南", "22");
            put("厦门", "15");
//            put("宁波", "11");
//            put("福州", "14");
//            put("青岛", "21");
//            put("合肥", "110");
//            put("常州", "93");

//            put("扬州", "12");
//            put("温州", "101");
//            put("绍兴", "104");
//            put("嘉兴", "102");
//            put("烟台", "148");
//            put("威海", "152");
//            put("镇江", "98");
//            put("南通", "94");
            //put("金华", "105");
//            put("徐州", "92");
//            put("潍坊", "149");
//            put("淄博", "145");
            //put("临沂", "155");
//            put("马鞍山", "114");
//            put("台州", "108");
//            put("泰州", "99");
//            put("济宁", "150");
//            put("泰安", "151");
//            //中部西部
            put("成都", "8");
            put("武汉", "16");
            //put("郑州", "6");
            //put("长沙", "344");
//            put("南昌", "134");
//            put("贵阳", "258");
//            put("西宁", "313");
            //put("重庆", "9");
            put("西安", "17");
//            put("昆明", "267");
//            put("兰州", "299");
//            put("乌鲁木齐", "325");
//            put("银川", "321");
            //华南地区
            put("广州", "4");
            put("深圳", "7");
            put("佛山", "208");
//            put("珠海", "206");
//            put("东莞", "219");
//            put("三亚", "345");
//            put("海口", "23");
//            put("南宁", "224");
//            put("惠州", "213");
        }
    };

    public void scratch(String cityId, String cityName) throws Exception {
        String nextPage = String.format("http://www.dianping.com/search/category/%s/20/g119p10", cityId);
        while (StringUtils.isNotEmpty(nextPage)) {
            try {
                String content = requestDianping(nextPage, 5);
                nextPage = null;
                if (content == null) continue;
                Document doc = Jsoup.parse(content);
                if (doc.getElementById("shop-all-list") != null) {
                    Elements shopListElements = doc.getElementById("shop-all-list").getElementsByTag("li");
                    for (Element shopElement : shopListElements) {
                        MallInfo mallInfo = new MallInfo();
                        try {
                            mallInfo.setCity(cityName);
                            String mallName = shopElement.getElementsByTag("img").attr("title");
                            mallInfo.setName(mallName);
                            mallInfo.setFullName(mallName);
                            int index = mallName.indexOf("(");
                            if (index > 0) {
                                String branch = mallName.substring(1 + index, mallName.indexOf(")"));
                                mallInfo.setName(mallName.substring(0, index));
                                mallInfo.setBranch(branch);
                            }
                            String mallPic = shopElement.getElementsByTag("img").attr("data-src");
                            mallInfo.setMallPic(mallPic);
                            String mallUrl = shopElement.getElementsByClass("tit").first().getElementsByTag("a").attr("href");
                            logger.info("fetch mall:" + mallName);
                            if (shopElement.getElementsByClass("comment-list").size() > 0) {
                                Elements commentElements = shopElement.getElementsByClass("comment-list").first().getElementsByTag("b");
                                mallInfo.setQuality(commentElements.get(0).text());
                                mallInfo.setEnvironment(commentElements.get(1).text());
                                mallInfo.setService(commentElements.get(2).text());
                            }
                            Elements addrElements = shopElement.getElementsByClass("tag-addr").first().getElementsByTag("span");
                            mallInfo.setCategory(addrElements.get(0).text());
                            mallInfo.setPosition(addrElements.get(1).text());
                            mallInfo.setAddress(addrElements.get(2).text());
                            String mallId = mallUrl.substring(1 + mallUrl.lastIndexOf('/'));
                            mallInfo.setMallId(mallId);
                            if (null != getMallDetail(mallUrl, mallInfo)) {
                                mallInfoMapper.add(mallInfo);
                                List<String> idList = mallInfo.getShopIdList();
                                int segment = idList.size() / 100 + 1;
                                for (index = 0; index < segment; index++) {
                                    int startIndex = 100 * index;
                                    int endIndex = index == (segment - 1) ? idList.size() : startIndex + 100;
                                    List<String> subList = idList.subList(startIndex, endIndex);
                                    List<ShopInfo> existShopList = shopInfoMapper.findByShopIdList(subList);
                                    List<String> existShopIdList = Lists.transform(existShopList, new Function<ShopInfo, String>() {
                                        @Override
                                        public String apply(ShopInfo shopInfo) {
                                            return shopInfo.getShopId();
                                        }
                                    });
                                    Collection notExistsShopId = CollectionUtils.subtract(subList, existShopIdList);
                                    Iterator iter = notExistsShopId.iterator();
                                    String shopId = null;
                                    while (iter.hasNext()) {
                                        try {
                                            shopId = (String) iter.next();
                                            ShopInfo shopInfo = getShopDetail(shopId);
                                            if (shopInfo != null) {
                                                shopInfo.setCity(cityName);
                                                shopInfo.setMallId(mallInfo.getMallId());
                                                shopInfo.setShopId(String.valueOf(shopId));
                                                shopInfoMapper.add(shopInfo);
                                                logger.info("add shop: {},{}", shopInfo.getBrandName(), shopInfo.getBranchName());
                                            }
                                        } catch (Exception e) {
                                            logger.error("fetch shop detail failed:" + shopId, e);
                                        }
                                    }
                                    CollectionUtils.filter(existShopList, new Predicate() {
                                        @Override
                                        public boolean evaluate(Object o) {
                                            String mallId = ((ShopInfo) o).getMallId();
                                            return StringUtils.isEmpty(mallId) || mallId.length() < 4;
                                        }
                                    });
                                    List<String> needUpdateShopIdList = Lists.transform(existShopList, new Function<ShopInfo, String>() {
                                        @Override
                                        public String apply(ShopInfo shopInfo) {
                                            return shopInfo.getShopId();
                                        }
                                    });
                                    if (needUpdateShopIdList.size() > 0) {
                                        Map<String, Object> params = Maps.newHashMap();
                                        params.put("shopIdList", needUpdateShopIdList);
                                        params.put("mallId", mallId);
                                        shopInfoMapper.updateByShopId(params);
                                        logger.info("update {} shops for mallId {}", existShopIdList.size(), mallId);
                                    }
                                }
                            }
                            logger.info("fetch {} shops for mall {}", mallInfo.getShopIdList().size(), mallName);
                        } catch (Exception e) {
                            logger.info("scratch mall failed:" + mallInfo.getMallId(), e);
                        }
                    }
                    Element nextPageElement = doc.getElementsByClass("page").last();
                    if (nextPageElement.getElementsByClass("next").size() > 0) {
                        nextPage = nextPageElement.getElementsByClass("next").first().attr("href");
                        logger.info("scratch page:" + nextPage);
                    }
                }
            } catch (Exception e1) {
                logger.error("", e1);
            }
        }

    }

    static MallInfo getMallDetail(String mallUrl, MallInfo mallInfo) {
        try {
            String content = requestDianping(mallUrl, 4);
            if (content != null) {
                String startStr = "facade({entry:\"app-main-mall-brands\", data: {\n" +
                        "brandList:";
                int index = content.indexOf(startStr);
                if (index > 0) {
                    String shopList = content.substring(startStr.length() + index, content.indexOf("facade({entry:\"app-main-mall-meta\"") - 5);
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map> list = mapper.readValue(shopList, List.class);
                    List<String> shopIdList = Lists.transform(list, new Function() {
                        @Override
                        public Object apply(Object o) {
                            Object object = ((Map) o).get("id");
                            return String.valueOf(object);
                        }
                    });
                    mallInfo.setBrandNum(shopIdList.size());
                    mallInfo.setShopIdList(shopIdList);
                    return mallInfo;
                }
            }
        } catch (Exception e) {
            logger.error("fetch mall detail failed:" + mallInfo.getName(), e);
        }
        return null;
    }

    public static String requestDianping(String url, int retryMaxCnt) throws InterruptedException {
        int retryCnt = 0;
        long sleepTimes = 1000;
        while (retryCnt < retryMaxCnt) {
            try {
                String content = (String) HttpRequestUtil.httpResponse(url, RequestHeader.dianpingHeaders);
                return content;
            } catch (Exception e) {
                logger.error("request failed", e);
                try {
                    Thread.sleep((long) (sleepTimes * Math.pow(2, retryCnt)));
                    String content = (String) HttpRequestUtil.submitRequestWithHeaders("http://ip4.hahado.cn/simple/switch-ip?username=ydzicycrgu&password=AkI3OByg6QYKJ", true, RequestHeader.requestHeaders);
                } catch (Exception e1) {

                }
                retryCnt++;
                logger.warn("prepare {}th more to request {}", retryCnt, url);
            }
        }
        return null;
    }

    static ShopInfo getShopDetail(String shopId) throws Exception {
        String content = requestDianping("http://www.dianping.com/shop/" + shopId, 4);
        if (content != null) {
            Document doc = Jsoup.parse(content);
            Element basicElement = doc.getElementById("basic-info");
            if (basicElement != null) {
                ShopInfo shopInfoDO = new ShopInfo();
                shopInfoDO.setShopId(String.valueOf(shopId));
                String shopName = basicElement.getElementsByClass("shop-name").get(0).text();
                Elements hrefElements = doc.getElementsByClass("breadcrumb").get(0).getElementsByTag("a");
                if (shopName.contains(hrefElements.last().text())) {
                    shopInfoDO.setCategory(hrefElements.get(hrefElements.size() - 2).text());
                } else {
                    shopInfoDO.setCategory(hrefElements.last().text());
                }
                if (shopName.indexOf("(") > 0) {
                    String branchName = shopName.substring(1 + shopName.indexOf("("), shopName.indexOf(")"));
                    shopName = shopName.substring(0, shopName.indexOf("("));
                    shopInfoDO.setBranchName(branchName);
                }
                shopInfoDO.setBrandName(shopName);//弥茶
                String star = basicElement.getElementsByClass("mid-rank-stars").get(0).attr("title");
                shopInfoDO.setScore(star);
                String address = basicElement.getElementsByClass("address").get(0).getElementsByClass("item").get(0).attr("title");
                shopInfoDO.setAddress(address);
                Elements commentElements = basicElement.getElementsByClass("item");
                for (Element element : commentElements) {
                    if (element.text().startsWith("产品：")) {
                        String quality = StringUtils.trim(element.text().substring("产品：".length()));
                        Double value = 10 * Double.parseDouble(quality);
                        shopInfoDO.setQuality(value.intValue());
                    } else if (element.text().startsWith("效果：")) {
                        String quality = StringUtils.trim(element.text().substring("效果：".length()));
                        Double value = 10 * Double.parseDouble(quality);
                        shopInfoDO.setQuality(value.intValue());
                    } else if (element.text().startsWith("口味：")) {
                        String quality = StringUtils.trim(element.text().substring("口味：".length()));
                        Double value = 10 * Double.parseDouble(quality);
                        shopInfoDO.setQuality(value.intValue());
                    } else if (element.text().startsWith("服务：")) {
                        String quality = StringUtils.trim(element.text().substring("服务：".length()));
                        Double value = 10 * Double.parseDouble(quality);
                        shopInfoDO.setService(value.intValue());
                    } else if (element.text().startsWith("环境：")) {
                        String quality = StringUtils.trim(element.text().substring("环境：".length()));
                        Double value = 10 * Double.parseDouble(quality);
                        shopInfoDO.setEnvironment(value.intValue());
                    } else if (element.text().startsWith("消费：") && element.text().indexOf('元') > 0) {
                        String quality = StringUtils.trim(element.text().substring("消费：".length(), element.text().indexOf('元')));
                        shopInfoDO.setAvgPrice(quality);
                    }
                }
                if (basicElement.getElementById("avgPriceTitle") != null) {
                    String consumeNum = basicElement.getElementById("avgPriceTitle").text();
                    if (consumeNum.indexOf("消费") >= 0 && consumeNum.indexOf('元') > 0) {
                        consumeNum = consumeNum.substring("消费：".length(), consumeNum.indexOf('元'));
                        shopInfoDO.setAvgPrice(consumeNum);
                    } else if (consumeNum.indexOf("人均") >= 0 && consumeNum.indexOf('元') > 0) {
                        consumeNum = consumeNum.substring("人均：".length(), consumeNum.indexOf('元'));
                        shopInfoDO.setAvgPrice(consumeNum);
                    }
                }
                if (basicElement.getElementsByClass("tel").size() > 0 && basicElement.getElementsByClass("tel").get(0).getElementsByClass("item").size() > 0) {
                    String tel = basicElement.getElementsByClass("tel").get(0).getElementsByClass("item").get(0).text();
                    shopInfoDO.setPhoneNo(tel);
                }
                if (content.indexOf("window.shop_config=") > 0) {
                    String config = content.substring("window.shop_config=".length() + content.indexOf("window.shop_config="));
                    config = config.substring(0, config.indexOf("</script>"));
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
                    mapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
                    Map<String, Object> map = mapper.readValue(config, Map.class);
                    String defaultPic = (String) map.get("defaultPic");
                    shopInfoDO.setDefaultPicKey(defaultPic);
                    String glat = (String) map.get("shopGlat");
                    shopInfoDO.setGlat(glat);
                    String glng = (String) map.get("shopGlng");
                    shopInfoDO.setGlng(glng);
                }
                return shopInfoDO;
            } else {
                ShopInfo shopInfoDO = new ShopInfo();
                shopInfoDO.setShopId(shopId);
                String shopName = null;
                if (doc.getElementsByClass("shop-title").size() > 0)
                    shopName = doc.getElementsByClass("shop-title").get(0).text();
                else if (doc.getElementsByClass("hotel-title").size() > 0)
                    shopName = doc.getElementsByClass("hotel-title").get(0).getElementsByTag("h1").get(0).text();
                Elements hrefElements = null;
                if (doc.getElementsByClass("breadcrumb").size() > 0)
                    hrefElements = doc.getElementsByClass("breadcrumb").get(0).getElementsByTag("a");
                else
                    hrefElements = doc.getElementsByClass("breadcrumb-wrapper").get(0).getElementsByTag("a");
                if (shopName.contains(hrefElements.last().text())) {
                    shopInfoDO.setCategory(hrefElements.get(hrefElements.size() - 2).text());
                } else {
                    shopInfoDO.setCategory(hrefElements.last().text());
                }
                if (shopName.indexOf("(") > 0) {
                    String branchName = shopName.substring(1 + shopName.indexOf("("), shopName.indexOf(")"));
                    shopName = shopName.substring(0, shopName.indexOf("("));
                    shopInfoDO.setBranchName(branchName);
                }
                shopInfoDO.setBrandName(shopName);//弥茶
                String address = null;
                if (doc.getElementsByClass("shop-info-inner").size() > 0) {
                    address = doc.getElementsByClass("shop-info-inner").get(0).getElementsByAttributeValue("itemprop", "street-address").text();
                }
                if (address == null && doc.getElementsByClass("address").size() > 0) {
                    Element addressEle = doc.getElementsByClass("address").get(0);
                    if (addressEle.getElementsByClass("region").size() > 0)
                        address = addressEle.getElementsByClass("region").get(0).text();
                }
                if (address == null && doc.getElementsByClass("shop-addr").size() > 0) {
                    Element addressEle = doc.getElementsByClass("shop-addr").get(0);
                    if (addressEle.getElementsByTag("span").size() > 0)
                        address = addressEle.getElementsByTag("span").get(0).attr("title");
                }
                if (address == null && doc.getElementsByClass("shop-addr").size() > 0) {
                    address = doc.getElementsByClass("shop-addr").get(0).getElementsByClass("fl").get(0).attr("title");
                }
                if (address == null && doc.getElementsByClass("hotel-address").size() > 0) {
                    address = doc.getElementsByClass("hotel-address").get(0).text();
                    address = address.substring(address.indexOf("地址：") + "地址：".length());
                }
                shopInfoDO.setAddress(address);
                return shopInfoDO;
            }
        }
        return null;
    }
}
