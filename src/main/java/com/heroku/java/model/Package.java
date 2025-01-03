package com.heroku.java.model;

import java.util.List;

public class Package {
    private Long packageId;
    private String packageName;
    private double packagePrice;
    private List<Activity> activities;
    private int activityCount;

    // Default constructor
    public Package() {}

    // Parameterized constructor
    public Package(Long packageId, String packageName, double packagePrice, List<Activity> activities) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.packagePrice = packagePrice;
        this.activities = activities;
        this.activityCount = activities != null ? activities.size() : 0;
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
        // int totalPrice = 0;
        // for (Activity act : this.activities) {
        //     totalPrice += act.getActivityPrice();
        // }
        // return totalPrice;
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

    public int getActivityCount() {
        return activityCount;
    }

    public void setActivityCount(int activityCount) {
        this.activityCount = activityCount;
    }
}

