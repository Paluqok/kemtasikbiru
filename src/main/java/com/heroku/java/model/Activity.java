package com.heroku.java.model;

public class Activity {
    private Long activityId;
    private String activityName;
    private String activityDuration;
    private double activityPrice;
    private String activityImage;

    // Default constructor
    public Activity() {
    }

    // Parameterized constructor
    public Activity(Long activityId, String activityName, String activityDuration, double activityPrice, String activityImage) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.activityDuration = activityDuration;
        this.activityPrice = activityPrice;
        this.activityImage = activityImage;
    }

    // Getters and setters
    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityDuration() {
        return activityDuration;
    }

    public void setActivityDuration(String activityDuration) {
        this.activityDuration = activityDuration;
    }

    public double getActivityPrice() {
        return activityPrice;
    }

    public void setActivityPrice(double activityPrice) {
        this.activityPrice = activityPrice;
    }

    public String getActivityImage() {
        return activityImage;
    }

    public void setActivityImage(String activityImage) {
        this.activityImage = activityImage;
    }
}
