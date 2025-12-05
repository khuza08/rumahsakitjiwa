package rumahsakitjiwa.controller;

import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.ConsultationSchedule;
import rumahsakitjiwa.utils.ConsultationScheduleHelper;
import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class ConsultationScheduleController {

    public boolean validateInput(JTextField txtConsultationDate, JTextField txtStartTime, JTextField txtEndTime,
                                 JComboBox<String> cbPatient, JComboBox<String> cbDoctor) {
        if (cbPatient.getSelectedItem() == null || cbPatient.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pasien harus dipilih!");
            cbPatient.requestFocus();
            return false;
        }

        if (cbDoctor.getSelectedItem() == null || cbDoctor.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Dokter harus dipilih!");
            cbDoctor.requestFocus();
            return false;
        }

        try {
            LocalDate consultationDate = LocalDate.parse(txtConsultationDate.getText().trim());
            if (consultationDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(null, "Tanggal konsultasi tidak valid (harus hari ini atau di masa depan)!");
                return false;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Format tanggal tidak valid! Gunakan format YYYY-MM-DD");
            return false;
        }

        try {
            LocalTime startTime = LocalTime.parse(txtStartTime.getText().trim());
            LocalTime endTime = LocalTime.parse(txtEndTime.getText().trim());
            if (startTime.isAfter(endTime)) {
                JOptionPane.showMessageDialog(null, "Waktu mulai harus sebelum waktu selesai!");
                return false;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Format waktu tidak valid! Gunakan format HH:MM");
            return false;
        }

        return true;
    }

    public boolean isDoctorAvailableForTime(String doctorCode, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Cek apakah dokter memiliki jadwal kerja di hari tersebut
            String dayOfWeek = ConsultationScheduleHelper.getIndonesianDay(date.getDayOfWeek().name());

            String sql = "SELECT s.shift, st.start_time, st.end_time FROM schedules s " +
                        "JOIN doctors d ON s.doctor_code = d.doctor_code " +
                        "JOIN shift_times st ON s.shift = st.shift_name " +
                        "WHERE d.doctor_code = ? AND FIND_IN_SET(?, s.days) > 0";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, doctorCode);
            pstmt.setString(2, dayOfWeek);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                return false; // Dokter tidak kerja di hari tersebut
            }

            // Cek apakah waktu yang diminta berada dalam waktu shift dokter
            LocalTime docStartTime = rs.getTime("start_time").toLocalTime();
            LocalTime docEndTime = rs.getTime("end_time").toLocalTime();

            if (startTime.isBefore(docStartTime) || endTime.isAfter(docEndTime)) {
                return false; // Waktu konsultasi di luar waktu shift dokter
            }

            // Cek apakah ada jadwal konsultasi lain di waktu yang sama
            String checkConflictSql = "SELECT COUNT(*) FROM consultation_schedules " +
                                    "WHERE doctor_code = ? AND consultation_date = ? " +
                                    "AND status != 'Cancelled' AND status != 'Completed' " +
                                    "AND ((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?))";

            PreparedStatement checkPstmt = conn.prepareStatement(checkConflictSql);
            checkPstmt.setString(1, doctorCode);
            checkPstmt.setDate(2, java.sql.Date.valueOf(date));
            checkPstmt.setTime(3, java.sql.Time.valueOf(endTime));
            checkPstmt.setTime(4, java.sql.Time.valueOf(startTime));
            checkPstmt.setTime(5, java.sql.Time.valueOf(endTime));
            checkPstmt.setTime(6, java.sql.Time.valueOf(startTime));

            ResultSet conflictRs = checkPstmt.executeQuery();
            if (conflictRs.next() && conflictRs.getInt(1) > 0) {
                return false; // Ada konflik jadwal
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isPatientBusyAtTime(String patientCode, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return isPatientBusyAtTime(patientCode, date, startTime, endTime, -1);
    }

    public boolean isPatientBusyAtTime(String patientCode, LocalDate date, LocalTime startTime, LocalTime endTime, int excludeScheduleId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            PreparedStatement pstmt;

            if (excludeScheduleId != -1) {
                sql = "SELECT COUNT(*) FROM consultation_schedules " +
                      "WHERE patient_code = ? AND consultation_date = ? AND id != ? " +
                      "AND status != 'Cancelled' AND status != 'Completed' " +
                      "AND ((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?))";

                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, patientCode);
                pstmt.setDate(2, java.sql.Date.valueOf(date));
                pstmt.setInt(3, excludeScheduleId);
            } else {
                sql = "SELECT COUNT(*) FROM consultation_schedules " +
                      "WHERE patient_code = ? AND consultation_date = ? " +
                      "AND status != 'Cancelled' AND status != 'Completed' " +
                      "AND ((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?))";

                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, patientCode);
                pstmt.setDate(2, java.sql.Date.valueOf(date));
            }

            pstmt.setTime(3, java.sql.Time.valueOf(endTime));
            pstmt.setTime(4, java.sql.Time.valueOf(startTime));
            pstmt.setTime(5, java.sql.Time.valueOf(endTime));
            pstmt.setTime(6, java.sql.Time.valueOf(startTime));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Ada konflik jadwal
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Asumsikan sibuk jika terjadi error
        }
        return false;
    }

    public String getIndonesianDay(String englishDay) {
        return ConsultationScheduleHelper.getIndonesianDay(englishDay);
    }
}