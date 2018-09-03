
package com.cangqing.spider.http;

/**
 * Created by song on 2016/11/30.
 */
public interface RequestCallBack<T> {
    T parse(String content);
}
