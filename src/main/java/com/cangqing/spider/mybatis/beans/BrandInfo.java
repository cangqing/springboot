package com.cangqing.spider.mybatis.beans;

/**
 * Created by song on 2017/7/10.
 */
public class BrandInfo {
    private Integer id;
    private String name;
    private String companyName;
    private String logo;
    private String shopMode;
    //开店数量
    private String shopNum;
    private String meanPrice;
    private String foundYear;
    private String category;
    //合作期限
    private String period;
    //需求面积
    private String areaRequired;
    private String frontPics;
    private String brandIntro;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getShopMode() {
        return shopMode;
    }

    public void setShopMode(String shopMode) {
        this.shopMode = shopMode;
    }

    public String getShopNum() {
        return shopNum;
    }

    public void setShopNum(String shopNum) {
        this.shopNum = shopNum;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getAreaRequired() {
        return areaRequired;
    }

    public void setAreaRequired(String areaRequired) {
        this.areaRequired = areaRequired;
    }

    public String getMeanPrice() {
        return meanPrice;
    }

    public void setMeanPrice(String meanPrice) {
        this.meanPrice = meanPrice;
    }

    public String getFoundYear() {
        return foundYear;
    }

    public void setFoundYear(String foundYear) {
        this.foundYear = foundYear;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFrontPics() {
        return frontPics;
    }

    public void setFrontPics(String frontPics) {
        this.frontPics = frontPics;
    }

    public String getBrandIntro() {
        return brandIntro;
    }

    public void setBrandIntro(String brandIntro) {
        this.brandIntro = brandIntro;
    }
}
