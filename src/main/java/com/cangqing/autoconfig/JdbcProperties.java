package com.cangqing.autoconfig;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;

/*****
 Project gag-spider
 @Author shenjia
 @Date 2018/8/31
 *****/

public class JdbcProperties {
    private final String name;
    private final String address;
    private final String username;
    private final String password;

    @JSONCreator
    public JdbcProperties(
            @JSONField(name = "name") String name,
            @JSONField(name = "address") String address,
            @JSONField(name = "username") String username,
            @JSONField(name = "password") String password) {
        this.name = name;
        this.address = address;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
