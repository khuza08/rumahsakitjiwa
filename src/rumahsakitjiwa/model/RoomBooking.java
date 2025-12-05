package rumahsakitjiwa.model;

import java.sql.Timestamp;

public class RoomBooking {
    private int id;
    private int patientId;
    private String patientCode;
    private String patientName;
    private int roomId;
    private String roomNumber;
    private String roomType;
    private Timestamp checkInDate;
    private Timestamp checkOutDate;
    private String status; // Booking, Check-in, Check-out, Cancelled
    private Timestamp bookingDate;
    private double totalCost;
    private String notes;

    // Constructors
    public RoomBooking() {}

    public RoomBooking(int id, int patientId, String patientCode, String patientName, 
                      int roomId, String roomNumber, String roomType, 
                      Timestamp checkInDate, Timestamp checkOutDate, String status, 
                      Timestamp bookingDate, double totalCost, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.patientCode = patientCode;
        this.patientName = patientName;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.bookingDate = bookingDate;
        this.totalCost = totalCost;
        this.notes = notes;
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

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public Timestamp getCheckInDate() { return checkInDate; }
    public void setCheckInDate(Timestamp checkInDate) { this.checkInDate = checkInDate; }

    public Timestamp getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(Timestamp checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getBookingDate() { return bookingDate; }
    public void setBookingDate(Timestamp bookingDate) { this.bookingDate = bookingDate; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}