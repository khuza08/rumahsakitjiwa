package rumahsakitjiwa.model;

import java.sql.Time;

public class Schedule {
    private int id;
    private int doctorId;
    private String dayOfWeek;
    private Time startTime;
    private Time endTime;
    private String location;
    private int maxPatients;
    private boolean isActive;
    
    // Default constructor
    public Schedule() {
    }
    
    // Parameterized constructor
    public Schedule(int id, int doctorId, String dayOfWeek, Time startTime, Time endTime, 
                   String location, int maxPatients, boolean isActive) {
        this.id = id;
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.maxPatients = maxPatients;
        this.isActive = isActive;
    }
    
    // Getter and Setter methods
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }
    
    public String getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public Time getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }
    
    public Time getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public int getMaxPatients() {
        return maxPatients;
    }
    
    public void setMaxPatients(int maxPatients) {
        this.maxPatients = maxPatients;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}