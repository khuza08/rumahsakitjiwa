package rumahsakitjiwa.model;

public class Schedule {
    private int id;
    private int doctorId;
    private String days;        // e.g., "Senin,Rabu"
    private String shift;       // e.g., "Pagi (06:00 - 14:00)"
    private java.sql.Time startTime;
    private java.sql.Time endTime;

    public Schedule() {}

    public Schedule(int id, int doctorId, String days, String shift, 
                    java.sql.Time startTime, java.sql.Time endTime) {
        this.id = id;
        this.doctorId = doctorId;
        this.days = days;
        this.shift = shift;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public java.sql.Time getStartTime() { return startTime; }
    public void setStartTime(java.sql.Time startTime) { this.startTime = startTime; }

    public java.sql.Time getEndTime() { return endTime; }
    public void setEndTime(java.sql.Time endTime) { this.endTime = endTime; }
}