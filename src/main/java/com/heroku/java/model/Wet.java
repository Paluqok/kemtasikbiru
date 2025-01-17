package com.heroku.java.model;

public class Wet extends Activity {
    private String activityEquipment;

    public Wet() {}

    public Wet(Long activityId, String activityName, String activityDuration, double activityPrice, String activityImagePath, String activityEquipment) {
        super(activityId, activityName, activityDuration, activityPrice, activityImagePath);
        this.activityEquipment = activityEquipment;
    }

    public String getActivityEquipment() { 
        return activityEquipment; 
        }
    public void setActivityEquipment(String activityEquipment) { 
        this.activityEquipment = activityEquipment; 
        }

    @Override
    public String toString() {
        return "Wet";
    }
}
 