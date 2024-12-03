package com.heroku.java.model;

import java.time.LocalDateTime;

public class Booking {
    private Long bookingId;
    private String bookingStatus;
    private Long staffId;
    private Long custId;
    private Long packageId;
    private LocalDateTime bookingStartDate;
    private LocalDateTime bookingEndDate;
    private String packageName;
    private double packagePrice;

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public LocalDateTime getBookingStartDate() {
        return bookingStartDate;
    }

    public void setBookingStartDate(LocalDateTime bookingStartDate) {
        this.bookingStartDate = bookingStartDate;
    }

    public LocalDateTime getBookingEndDate() {
        return bookingEndDate;
    }

    public void setBookingEndDate(LocalDateTime bookingEndDate) {
        this.bookingEndDate = bookingEndDate;
    }

    public void setPackageName(String packageName){
        this.packageName=packageName;
    }

    public String getPackageName(){
        return packageName;
    }

    public void setPackagePrice(double packagePrice){
        this.packagePrice=packagePrice;
    }

    public double getPackagePrice(){
        return packagePrice;
    }
 
}
