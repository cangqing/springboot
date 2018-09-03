package com.cangqing.spider.mybatis.beans;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by song on 2017/8/3.
 */
public class MallInfo {
    String id;
    String mallId;
    String name;
    String fullName;
    String branch;
    String address;
    String city;
    Integer brandNum;
    String quality;
    String environment;
    String service;
    String mallPic;
    String category;
    String position;
    List<String> shopIdList= Lists.newArrayList();

    public String getMallId() {
        return mallId;
    }

    public void setMallId(String mallId) {
        this.mallId = mallId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Integer getBrandNum() {
        return brandNum;
    }

    public void setBrandNum(Integer brandNum) {
        this.brandNum = brandNum;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMallPic() {
        return mallPic;
    }

    public void setMallPic(String mallPic) {
        this.mallPic = mallPic;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<String> getShopIdList() {
        return shopIdList;
    }

    public void setShopIdList(List<String> shopIdList) {
        this.shopIdList = shopIdList;
    }
}
