package com.cangqing.spider.controller;

import com.cangqing.spider.scracher.DianPingCommentScratcher;
import com.cangqing.spider.scracher.DianPingInfoScratcher;
import com.cangqing.spider.scracher.SentimentScratcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by song on 2017/8/29.
 */
@RestController
public class ScratchController {
    Logger logger = LoggerFactory.getLogger(ScratchController.class);

    @Autowired
    DianPingInfoScratcher dianPingInfoScratcher;
    @Autowired
    SentimentScratcher sentimentScratcher;
    @Autowired
    DianPingCommentScratcher dianPingCommentScratcher;

    @RequestMapping("/ok")
    public String ok() {
        return "ok";
    }

    @ResponseBody
    @RequestMapping(value = "/dianping/{cityId}/{cityName}", method = RequestMethod.GET)
    public String dianping(HttpServletRequest request, @PathVariable String cityId, @PathVariable String cityName, HttpServletResponse response, Model model) throws Exception {
       dianPingInfoScratcher.scratch(cityId, cityName);
        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/dianping", method = RequestMethod.GET)
    public String alldianping(HttpServletRequest request, Model model) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, String> entry : DianPingInfoScratcher.cityMap.entrySet()) {
                    logger.info("fetch city {}", entry.getKey());
                    try {
                        dianPingInfoScratcher.scratch(entry.getValue(), entry.getKey());
                    } catch (Exception e) {

                    }
                }
            }
        }).start();
        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/comment", method = RequestMethod.GET)
    public String comment(HttpServletRequest request, HttpServletResponse response, Model model) {
        dianPingCommentScratcher.scratch();
        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/sentiment", method = RequestMethod.GET)
    public String sentiment(HttpServletRequest request, HttpServletResponse response, Model model) {
        sentimentScratcher.scratch();
        return "OK";
    }
}
