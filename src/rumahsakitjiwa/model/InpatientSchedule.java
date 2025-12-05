package rumahsakitjiwa.model;

import java.sql.Timestamp;

public class InpatientSchedule {
    private int id;
    private int patientId;
    private String patientCode;
    private String patientName;
    private int doctorId;
    private String doctorCode;
    private String doctorName;
    private int roomId;
    private String roomNumber;
    private String roomType;
    private Timestamp admissionDate;
    private Timestamp dischargeDate;
    private String status; // Admitted, Discharged, Scheduled
    private Timestamp scheduledAt;
    private double totalCost;
    private String notes;
    private String diagnosis;

    // Constructors
    public InpatientSchedule() {}

    public InpatientSchedule(int id, int patientId, String patientCode, String patientName,
                            int doctorId, String doctorCode, String doctorName,
                            int roomId, String roomNumber, String roomType,
                            Timestamp admissionDate, Timestamp dischargeDate,
                            String status, Timestamp scheduledAt, double totalCost,
                            String notes, String diagnosis) {
        this.id = id;
        this.patientId = patientId;
        this.patientCode = patientCode;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorCode = doctorCode;
        this.doctorName = doctorName;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.totalCost = totalCost;
        this.notes = notes;
        this.diagnosis = diagnosis;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getDoctorCode() { return doctorCode; }
    public void setDoctorCode(String doctorCode) { this.doctorCode = doctorCode; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public Timestamp getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(Timestamp admissionDate) { this.admissionDate = admissionDate; }

    public Timestamp getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(Timestamp dischargeDate) { this.dischargeDate = dischargeDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Timestamp scheduledAt) { this.scheduledAt = scheduledAt; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
}