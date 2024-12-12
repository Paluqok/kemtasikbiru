package com.heroku.java.model;

public class Dry extends Activity {
    private String activityLocation;

    public Dry() {}

    public Dry(Long activityId, String activityName, String activityDuration, double activityPrice, String activityImagePath, String activityLocation) {
        super(activityId, activityName, activityDuration, activityPrice, activityImagePath);
        this.activityLocation = activityLocation;
    }

    public String getActivityLocation() { return activityLocation; }
    public void setActivityLocation(String activityLocation) { this.activityLocation = activityLocation; }

    @Override
    public String toString() {
        return "Dry";
    }
}
