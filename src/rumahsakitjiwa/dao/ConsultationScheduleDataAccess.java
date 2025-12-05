package rumahsakitjiwa.dao;

import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.ConsultationSchedule;
import rumahsakitjiwa.utils.ConsultationScheduleHelper;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class ConsultationScheduleDataAccess {

    public boolean insertSchedule(ConsultationSchedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO consultation_schedules (patient_code, patient_name, doctor_code, doctor_name, consultation_date, start_time, end_time, status, notes, room, inpatient_required, recommended_room_type, recommended_duration, admission_notes, scheduled_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getPatientCode());
            pstmt.setString(2, schedule.getPatientName());
            pstmt.setString(3, schedule.getDoctorCode());
            pstmt.setString(4, schedule.getDoctorName());
            pstmt.setDate(5, new java.sql.Date(schedule.getConsultationDate().getTime()));
            pstmt.setString(6, schedule.getStartTime());
            pstmt.setString(7, schedule.getEndTime());
            pstmt.setString(8, schedule.getStatus());
            pstmt.setString(9, schedule.getNotes());
            pstmt.setString(10, schedule.getRoom());
            pstmt.setBoolean(11, schedule.isInpatientRequired());
            pstmt.setString(12, schedule.getRecommendedRoomType());
            pstmt.setString(13, schedule.getRecommendedDuration());
            pstmt.setString(14, schedule.getAdmissionNotes());
            pstmt.setTimestamp(15, new java.sql.Timestamp(System.currentTimeMillis()));

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateScheduleInDB(ConsultationSchedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE consultation_schedules SET patient_code=?, patient_name=?, doctor_code=?, doctor_name=?, consultation_date=?, start_time=?, end_time=?, status=?, notes=?, room=?, inpatient_required=?, recommended_room_type=?, recommended_duration=?, admission_notes=?, scheduled_at=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getPatientCode());
            pstmt.setString(2, schedule.getPatientName());
            pstmt.setString(3, schedule.getDoctorCode());
            pstmt.setString(4, schedule.getDoctorName());
            pstmt.setDate(5, new java.sql.Date(schedule.getConsultationDate().getTime()));
            pstmt.setString(6, schedule.getStartTime());
            pstmt.setString(7, schedule.getEndTime());
            pstmt.setString(8, schedule.getStatus());
            pstmt.setString(9, schedule.getNotes());
            pstmt.setString(10, schedule.getRoom());
            pstmt.setBoolean(11, schedule.isInpatientRequired());
            pstmt.setString(12, schedule.getRecommendedRoomType());
            pstmt.setString(13, schedule.getRecommendedDuration());
            pstmt.setString(14, schedule.getAdmissionNotes());
            pstmt.setTimestamp(15, new java.sql.Timestamp(System.currentTimeMillis()));
            pstmt.setInt(16, schedule.getId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deleteScheduleFromDB(int scheduleId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM consultation_schedules WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, scheduleId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void loadSchedules(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM consultation_schedules ORDER BY consultation_date DESC, start_time";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("patient_code"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_code"),
                    rs.getString("doctor_name"),
                    rs.getDate("consultation_date"),
                    rs.getString("start_time") + " - " + rs.getString("end_time"),
                    rs.getString("status"),
                    rs.getString("room"),
                    // Handle kolom baru yang mungkin belum ada di database
                    ConsultationScheduleHelper.getSafeBooleanValue(rs, "inpatient_required"),
                    ConsultationScheduleHelper.getSafeStringValue(rs, "recommended_room_type"),
                    ConsultationScheduleHelper.getSafeStringValue(rs, "recommended_duration"),
                    ConsultationScheduleHelper.getSafeStringValue(rs, "admission_notes")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal memuat data jadwal: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}