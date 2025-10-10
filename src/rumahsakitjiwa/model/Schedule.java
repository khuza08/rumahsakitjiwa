package rumahsakitjiwa.model;

public class Schedule {
    private int id;
    private String doctorCode;   // ← GANTI DARI int doctorId
    private String days;
    private String shift;
    private java.sql.Time startTime;
    private java.sql.Time endTime;

    public Schedule() {}

    public Schedule(int id, String doctorCode, String days, String shift, 
                    java.sql.Time startTime, java.sql.Time endTime) {
        this.id = id;
        this.doctorCode = doctorCode;
        this.days = days;
        this.shift = shift;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDoctorCode() { return doctorCode; }
    public void setDoctorCode(String doctorCode) { this.doctorCode = doctorCode; }

    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public java.sql.Time getStartTime() { return startTime; }
    public void setStartTime(java.sql.Time startTime) { this.startTime = startTime; }

    public java.sql.Time getEndTime() { return endTime; }
    public void setEndTime(java.sql.Time endTime) { this.endTime = endTime; }
}