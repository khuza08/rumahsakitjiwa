package rumahsakitjiwa.model;

import java.sql.Timestamp;

public class ConsultationSchedule {
    private int id;
    private int patientId;
    private String patientCode;
    private String patientName;
    private int doctorId;
    private String doctorCode;
    private String doctorName;
    private Timestamp consultationDate;
    private String startTime;
    private String endTime;
    private String status; // Scheduled, Completed, Cancelled, No-show
    private Timestamp scheduledAt;
    private String notes;
    private String room; // Ruang konsultasi
    private boolean inpatientRequired;
    private String recommendedRoomType;
    private String recommendedDuration;
    private String admissionNotes;

    // Constructors
    public ConsultationSchedule() {}

    public ConsultationSchedule(int id, int patientId, String patientCode, String patientName,
                               int doctorId, String doctorCode, String doctorName,
                               Timestamp consultationDate, String startTime, String endTime,
                               String status, Timestamp scheduledAt, String notes, String room) {
        this.id = id;
        this.patientId = patientId;
        this.patientCode = patientCode;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorCode = doctorCode;
        this.doctorName = doctorName;
        this.consultationDate = consultationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
        this.room = room;
    }

    // Constructor yang lebih lengkap
    public ConsultationSchedule(int id, int patientId, String patientCode, String patientName,
                               int doctorId, String doctorCode, String doctorName,
                               Timestamp consultationDate, String startTime, String endTime,
                               String status, Timestamp scheduledAt, String notes, String room,
                               boolean inpatientRequired, String recommendedRoomType,
                               String recommendedDuration, String admissionNotes) {
        this.id = id;
        this.patientId = patientId;
        this.patientCode = patientCode;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorCode = doctorCode;
        this.doctorName = doctorName;
        this.consultationDate = consultationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
        this.room = room;
        this.inpatientRequired = inpatientRequired;
        this.recommendedRoomType = recommendedRoomType;
        this.recommendedDuration = recommendedDuration;
        this.admissionNotes = admissionNotes;
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

    public Timestamp getConsultationDate() { return consultationDate; }
    public void setConsultationDate(Timestamp consultationDate) { this.consultationDate = consultationDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Timestamp scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public boolean isInpatientRequired() { return inpatientRequired; }
    public void setInpatientRequired(boolean inpatientRequired) { this.inpatientRequired = inpatientRequired; }

    public String getRecommendedRoomType() { return recommendedRoomType; }
    public void setRecommendedRoomType(String recommendedRoomType) { this.recommendedRoomType = recommendedRoomType; }

    public String getRecommendedDuration() { return recommendedDuration; }
    public void setRecommendedDuration(String recommendedDuration) { this.recommendedDuration = recommendedDuration; }

    public String getAdmissionNotes() { return admissionNotes; }
    public void setAdmissionNotes(String admissionNotes) { this.admissionNotes = admissionNotes; }
}