package com.cangqing.spider.scracher;

import com.cangqing.spider.http.HttpRequestUtil;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by song on 2017/9/18.
 */
public class RequestHeader {

    public static Map<String, String> requestHeaders = new HashMap() {
        {
            put("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            put("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
            put("Content-Type", "text/xml;charset=UTF-8");
            put("Accept-Encoding", "gzip, deflate");
            put("Accept-Language", "zh-cn,zh;q=0.5");
            put("Connection", "keep-alive");
            put("Cache-Control", "max-age=0");
            put("Upgrade-Insecure-Requests", "1");
            put("Proxy-Switch-Ip", "yes");
            put("Proxy-Authorization", "Basic " + Base64.encode((HttpRequestUtil.proxyUser + ":" + HttpRequestUtil.proxyPass).getBytes()));
            //put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
        }
    };

    public static Map<String, String> dianpingHeaders = new HashMap() {
        {
            put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            put("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
            put("Content-Type", "text/xml;charset=UTF-8");
            put("Accept-Encoding", "gzip, deflate");
            put("Connection", "keep-alive");
            put("Cache-Control", "max-age=0");
            put("Host", "www.dianping.com");
            put("Proxy-Switch-Ip", "yes");
            put("Proxy-Authorization", "Basic " + Base64.encode((HttpRequestUtil.proxyUser + ":" + HttpRequestUtil.proxyPass).getBytes()));
            //put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        }
    };
    public static Map<String, String> weixinhHders= new HashMap() {
        {
            put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            put("Accept-Encoding", "gzip, deflate");
            put("Content-Type", "text/xml;charset=UTF-8");
            put("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
            put("Connection", "keep-alive");
            put("Host", "weixin.sogou.com");
            put("Cache-Control", "max-age=0");
            put("Upgrade-Insecure-Requests", "1");
            put("Proxy-Switch-Ip", "yes");
            put("Proxy-Authorization", "Basic " + Base64.encode((HttpRequestUtil.proxyUser + ":" + HttpRequestUtil.proxyPass).getBytes()));
            put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
        }
    };
}
