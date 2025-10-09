package rumahsakitjiwa.model;

public class Schedule {
    private int id;
    private int doctorId;
    private String days;        // e.g., "Senin,Selasa"
    private String shift;       // e.g., "Pagi (06:00 - 14:00)"
    private String location;
    private int maxPatients;
    
    // Default constructor
    public Schedule() {
    }
    
    // Parameterized constructor
    public Schedule(int id, int doctorId, String days, String shift, 
                    String location, int maxPatients) {
        this.id = id;
        this.doctorId = doctorId;
        this.days = days;
        this.shift = shift;
        this.location = location;
        this.maxPatients = maxPatients;
    }
    
    // Getters and Setters
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

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
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
}