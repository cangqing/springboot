package com.cangqing.spider.mybatis.beans;

/**
 * Created by song on 2017/7/10.
 */
public class EnterpriseInfo {
    private String id;
    private String entName;
    private String custTaxNo;
    private String address;
    private String telephone;
    private String custBank;
    private String bankAccount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntName() {
        return entName;
    }

    public void setEntName(String entName) {
        this.entName = entName;
    }

    public String getCustTaxNo() {
        return custTaxNo;
    }

    public void setCustTaxNo(String custTaxNo) {
        this.custTaxNo = custTaxNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCustBank() {
        return custBank;
    }

    public void setCustBank(String custBank) {
        this.custBank = custBank;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
}
