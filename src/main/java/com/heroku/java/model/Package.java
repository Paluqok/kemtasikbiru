package com.heroku.java.model;

import java.util.List;

public class Package {
    private Long packageId;
    private String packageName;
    private double packagePrice;
    private List<Activity> activities;

    // Default constructor
    public Package() {}

    // Parameterized constructor
    public Package(Long packageId, String packageName, double packagePrice, List<Activity> activities) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.packagePrice = packagePrice;
        this.activities = activities;
    }

    // Getters and setters
    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public double getPackagePrice() {
        return packagePrice;
    }

    public void setPackagePrice(double packagePrice) {
        this.packagePrice = packagePrice;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}

