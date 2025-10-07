package rumahsakitjiwa.model;

public class Doctor {
    private int id;
    private String doctorCode;
    private String fullName;
    private String specialization;
    private String phone;
    private String email;
    private String schedule;
    private String address;
    private boolean isActive;

    // Constructors
    public Doctor() {}

    public Doctor(int id, String doctorCode, String fullName, String specialization, 
                 String phone, String email, String schedule, boolean isActive) {
        this.id = id;
        this.doctorCode = doctorCode;
        this.fullName = fullName;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.schedule = schedule;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDoctorCode() {
        return doctorCode;
    }

    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", doctorCode='" + doctorCode + '\'' +
                ", fullName='" + fullName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", schedule='" + schedule + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}