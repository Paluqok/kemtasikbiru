package com.heroku.java.model;

public class Customer {
    private Long custId;
    private String custName;
    private String custEmail;
    private String custPhoneNo;
    private String custAddress;
    private String custPassword;

    public Customer() {}

    public Customer(Long custId, String custName, String custEmail, String custPhoneNo, String custAddress, String custPassword) {
        this.custId = custId;
        this.custName = custName;
        this.custEmail = custEmail;
        this.custPhoneNo = custPhoneNo;
        this.custAddress = custAddress;
        this.custPassword = custPassword;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getCustEmail() {
        return custEmail;
    }

    public void setCustEmail(String custEmail) {
        this.custEmail = custEmail;
    }

    public String getCustPhoneNo() {
        return custPhoneNo;
    }

    public void setCustPhoneNo(String custPhoneNo) {
        this.custPhoneNo = custPhoneNo;
    }

    public String getCustAddress() {
        return custAddress;
    }

    public void setCustAddress(String custAddress) {
        this.custAddress = custAddress;
    }

    public String getCustPassword() {
        return custPassword;
    }

    public void setCustPassword(String custPassword) {
        this.custPassword = custPassword;
    }
}
