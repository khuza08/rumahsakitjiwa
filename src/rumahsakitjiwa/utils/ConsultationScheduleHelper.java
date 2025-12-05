package rumahsakitjiwa.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;

public class ConsultationScheduleHelper {

    public static String getIndonesianDay(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "Senin";
            case TUESDAY: return "Selasa";
            case WEDNESDAY: return "Rabu";
            case THURSDAY: return "Kamis";
            case FRIDAY: return "Jumat";
            case SATURDAY: return "Sabtu";
            case SUNDAY: return "Minggu";
            default: return dayOfWeek.name();
        }
    }

    public static String getIndonesianDay(String englishDay) {
        switch (englishDay) {
            case "MONDAY": return "Senin";
            case "TUESDAY": return "Selasa";
            case "WEDNESDAY": return "Rabu";
            case "THURSDAY": return "Kamis";
            case "FRIDAY": return "Jumat";
            case "SATURDAY": return "Sabtu";
            case "SUNDAY": return "Minggu";
            default: return englishDay;
        }
    }

    public static boolean getSafeBooleanValue(ResultSet rs, String columnName) {
        try {
            return rs.getBoolean(columnName);
        } catch (SQLException e) {
            // Kolom mungkin tidak ditemukan, kembalikan default false
            return false;
        }
    }

    public static String getSafeStringValue(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            // Kolom mungkin tidak ditemukan, kembalikan string kosong
            return "";
        }
    }
}