package com.cangqing.spider.scracher;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cangqing.spider.http.HttpRequestUtil;
import com.cangqing.spider.http.TextExtractor;
import com.cangqing.spider.mybatis.beans.BrandAlias;
import com.cangqing.spider.mybatis.beans.BrandInfo;
import com.cangqing.spider.mybatis.beans.BrandSentiment;
import com.cangqing.spider.mybatis.beans.MallBrandQuery;
import com.cangqing.spider.mybatis.mapper.BrandAliasMapper;
import com.cangqing.spider.mybatis.mapper.BrandInfoMapper;
import com.cangqing.spider.mybatis.mapper.BrandSentimentMapper;
import com.cangqing.spider.mybatis.mapper.MallBrandQueryMapper;
import com.cangqing.spider.service.MailService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 新闻抓取
 * Created by song on 2017/8/17.
 */
@Component
public class SentimentScratcher {
    Logger logger = LoggerFactory.getLogger(SentimentScratcher.class);
    @Autowired
    BrandAliasMapper brandAliasMapper;
    @Autowired
    BrandInfoMapper brandInfoMapper;
    @Autowired
    MallBrandQueryMapper mallBrandQueryMapper;
    @Autowired
    BrandSentimentMapper brandSentimentMapper;
    @Autowired
    MailService mailService;

    ObjectMapper mapper = new ObjectMapper();

    String datePattern = "yyyy年MM月dd日 HH:mm";

    ThreadPoolExecutor scratchkExecutors = new ThreadPoolExecutor(
            5,
            9,
            1000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>()
    );


    //@Scheduled(cron = "0/3 * * * * *")
    public void test() {
        logger.info("test schedule");
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void scratch() {
        Map<String, Object> params = Maps.newHashMap();
//        params.put("mallId", "19TJRVRADR4KUL2IAG0765M9UE5TCHGM");
        params.put("offset", 0);
        params.put("size", 40000);
        List<MallBrandQuery> mallBrandQueryList = mallBrandQueryMapper.find(params);
        mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        mapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        int count = 0;
        for (final MallBrandQuery mallBrandQuery : mallBrandQueryList) {
//            if (!"1044".equals(mallBrandQuery.getBrandId()))
//                continue;
            final BrandInfo brandInfo = brandInfoMapper.findById(mallBrandQuery.getBrandId());
            if(brandInfo==null)
                continue;
            List<BrandAlias> brandAliasList = brandAliasMapper.findByBrandId(brandInfo.getId());
            final List<String> queryBrandList = Lists.newArrayList();
            queryBrandList.add(brandInfo.getName());
            for (BrandAlias brandAlias : brandAliasList)
                queryBrandList.add(brandAlias.getAlias());
            scratchkExecutors.submit(new Runnable() {
                @Override
                public void run() {
                    searchFromBaidu(brandInfo.getId(), queryBrandList);
                    //searchFromWeixin(brandInfo.getId(), queryBrandList);
                }
            });
            count++;
            if (count % 10 == 0) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {

                }
            }
        }
    }

    public void searchFromBaidu(Integer brandId, List<String> queryBrandList) {
        for (String brandName : queryBrandList) {
            try {
                String url = "http://news.baidu.com/ns?ct=0&clk=sortbytime&rn=10&ie=utf-8&rsv_bp=1&sr=0&cl=2&f=8&prevct=no&tn=news&word=" + URLEncoder.encode(brandName, "UTF-8");
                String content = request(url, RequestHeader.requestHeaders, false);
                Document doc = Jsoup.parse(content);
                Elements list = doc.getElementsByClass("result");
                logger.info("fetch {} news about brand {}", list.size(), brandName);
                for (Element record : list) {
                    try {
                        Elements hrefElements = record.getElementsByTag("a");
                        String title = hrefElements.first().html();
                        String link = hrefElements.first().attr("href");
                        Element summaryElement = record.getElementsByClass("c-summary").first();
                        if (summaryElement.getElementsByClass("c-span18").size() > 0) {
                            summaryElement = summaryElement.getElementsByClass("c-span18").first();
                        }
                        String summary = summaryElement.html().substring(summaryElement.getElementsByClass("c-author").first().outerHtml().length(), summaryElement.html().indexOf("<span class=\"c-info\">"));
                        String authorInfo = summaryElement.getElementsByClass("c-author").text();
                        //扩展 ASCII 码 空格 &nbsp;
                        int blankPos = authorInfo.indexOf(160);
                        String source = authorInfo.substring(0, blankPos);
                        String dateStr = authorInfo.substring(2 + blankPos);
                        Date publishDate = null;
                        if (dateStr.indexOf("分钟前") > 0) {
                            publishDate = new Date(System.currentTimeMillis() - 60L * 1000L * Integer.parseInt(dateStr.substring(0, dateStr.indexOf("分钟前"))));
                        } else if (dateStr.indexOf("小时前") > 0) {
                            publishDate = new Date(System.currentTimeMillis() - 3600L * 1000L * Integer.parseInt(dateStr.substring(0, dateStr.indexOf("小时前"))));
                        } else {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
                            publishDate = simpleDateFormat.parse(dateStr);
                            Calendar c = Calendar.getInstance();
                            c.add(Calendar.YEAR, -1); //年份减1
                            Date oneYearDay = c.getTime();
                            if (publishDate.before(oneYearDay)) {
                                break;
                            }
                        }
                        if (publishDate.after(new Date())) {
                            logger.error("{},{}", publishDate, authorInfo);
                            continue;
                        }
                        BrandSentiment brandSentiment = new BrandSentiment();
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        BASE64Encoder base64en = new BASE64Encoder();
                        //加密后的字符串
                        String summaryMd5 = base64en.encode(md5.digest(link.getBytes("utf-8")));
                        brandSentiment.setMd5(summaryMd5);
                        brandSentiment.setSource(source);
                        brandSentiment.setBrand(brandName);
                        brandSentiment.setQueryWord(brandName);
                        brandSentiment.setSite("baidu");
                        brandSentiment.setTitle(title);
                        brandSentiment.setLink(link);
                        String text = TextExtractor.extractTextFromUrl(link, RequestHeader.requestHeaders, false);
                        if (StringUtils.isEmpty(text))
                            continue;
                        brandSentiment.setSummary(summary);
                        brandSentiment.setContent(text);
                        brandSentiment.setPublishTime(publishDate);
                        brandSentiment.setBrandId(brandId);
                        brandSentimentMapper.add(brandSentiment);
                    } catch (Exception e) {
                        logger.error("scratch failed:" + url, e);
                    }
                }
            } catch (Exception e) {
                logger.error("fetch news from baidu failed:" + brandName, e);
            }
        }
    }

    void searchFromWeixin(Integer brandId, List<String> queryBrandList) {
        for (String brandName : queryBrandList) {
            try {
                String url = "http://weixin.sogou.com/weixin?type=2&query=" + URLEncoder.encode(brandName, "UTF-8");
                String content = request(url, RequestHeader.weixinhHders, true);
                Document doc = Jsoup.parse(content);
                Elements list = doc.getElementsByClass("txt-box");
                for (Element record : list) {
                    try {
                        String link = record.getElementsByTag("a").first().attr("href");
                        String title = record.getElementsByTag("a").first().html();
                        String summary = record.getElementsByClass("txt-info").first().html();
                        String dateScript = record.getElementsByClass("s2").first().html();
                        String startStr = "<script>document.write(timeConvert('";
                        String seconds = dateScript.substring(startStr.length(), dateScript.indexOf("'))</script>"));
                        Date publishDate = new Date(1000 * Long.parseLong(seconds));
                        BrandSentiment brandSentiment = new BrandSentiment();
                        String source = record.getElementsByClass("account").first().text();
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.YEAR, -1); //年份减1
                        Date oneYearDay = c.getTime();
                        if (publishDate.before(oneYearDay))
                            break;
                        brandSentiment.setPublishTime(publishDate);
                        logger.info("extract text:" + link);
                        String text = TextExtractor.extractTextFromUrl(link, RequestHeader.weixinhHders, false);
                        if (StringUtils.isEmpty(text))
                            continue;
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        BASE64Encoder base64en = new BASE64Encoder();
                        //加密后的字符串
                        String summaryMd5 = base64en.encode(md5.digest(summary.getBytes("utf-8")));
                        brandSentiment.setMd5(summaryMd5);
                        brandSentiment.setSource(source);
                        brandSentiment.setSite("weixin");
                        brandSentiment.setLink(link);
                        brandSentiment.setBrand(brandName);
                        brandSentiment.setQueryWord(brandName);
                        brandSentiment.setTitle(title);
                        brandSentiment.setSummary(summary);
                        brandSentiment.setContent(text);
                        brandSentiment.setBrandId(brandId);
                        brandSentimentMapper.add(brandSentiment);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        logger.error("fetch sentiment failed of " + brandName, e);
                    }
                }
            } catch (Exception e) {
                logger.error("fetch sentiment failed of " + brandName, e);
            }
        }
    }

    void delayRequest(long sleepTimes, boolean useProxy) throws InterruptedException {
        String content = (String) HttpRequestUtil.submitRequestWithHeaders("http://ip4.hahado.cn/simple/switch-ip?username=ydzicycrgu&password=AkI3OByg6QYKJ", useProxy, RequestHeader.requestHeaders);
        if (content.contains("false"))
            Thread.sleep(sleepTimes);
    }

    String request(String url, Map<String, String> requestHeaders, boolean useProxy) throws InterruptedException {
        int retryCnt = 0;
        long sleepTimes = 1000;
        while (retryCnt <= 3) {
            try {
                String content = (String) HttpRequestUtil.submitRequestWithHeaders(url, useProxy, requestHeaders);
                if (content != null && content.contains("访问过于频繁"))
                    throw new RuntimeException("请求频繁");
                return content;
            } catch (Exception e) {
                logger.error("request failed", e);
                sleepTimes = (long) (sleepTimes * Math.pow(2, retryCnt));
                delayRequest(sleepTimes, useProxy);
            }
            retryCnt++;
            logger.warn("prepare {}th more to request {}", retryCnt, url);
        }
        return null;
    }
}
