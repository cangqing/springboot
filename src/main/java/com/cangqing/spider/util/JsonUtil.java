package com.cangqing.spider.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by song on 2016/11/30.
 */
public class JsonUtil {

    public static <T> T toObj(String json, Class<T> clazz) {
        if (StringUtils.isNotEmpty(json)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(json, clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<String> toList(String json) {
        if (StringUtils.isNotEmpty(json)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(json, List.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    public static Map<String, Object> toMap(String json) {
        if (StringUtils.isNotEmpty(json)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(json, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyMap();
    }

    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null)
            return null;

        Object obj = beanClass.newInstance();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            Method setter = property.getWriteMethod();
            if (setter != null) {
                setter.invoke(obj, map.get(property.getName()));
            }
        }
        return obj;
    }

    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (obj == null)
            return null;

        Map<String, Object> map = new HashMap<String, Object>();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            if (key.compareToIgnoreCase("class") == 0) {
                continue;
            }
            Method getter = property.getReadMethod();
            Object value = getter != null ? getter.invoke(obj) : null;
            map.put(key, value);
        }
        return map;
    }

    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static String toFullJson(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
    }

    public static void main(String args[]) throws IOException {
        String value = "{\"lngnum\":12,\"dbnum\":12.34,\"dbnum\":12.35,\"datetime\":\"2014-12-23 12:09:21\",\"date\":\"2014-12-23\"}";
        //Map map = JsonUtils.toMap();
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(value, Map.class);
        System.out.println(map);
    }
}
