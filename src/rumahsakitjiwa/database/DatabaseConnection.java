package rumahsakitjiwa.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/rumahsakitjiwa";
    private static final String USERNAME = "elza";
    private static final String PASSWORD = "elza";

    // Method untuk mendapatkan koneksi database
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver tidak ditemukan", e);
        }
    }

    
    // Method untuk test koneksi
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Koneksi database gagal: " + e.getMessage());
            return false;
        }
    }
    
    // Method untuk mendapatkan informasi database
    public static void printDatabaseInfo() {
        try (Connection conn = getConnection()) {
            System.out.println("=== INFORMASI DATABASE ===");
            System.out.println("URL: " + URL);
            System.out.println("Username: " + USERNAME);
            System.out.println("Status: " + (conn.isClosed() ? "Tertutup" : "Terhubung"));
            System.out.println("Database: " + conn.getCatalog());
            System.out.println("==========================");
        } catch (SQLException e) {
            System.err.println("Error mendapatkan info database: " + e.getMessage());
        }
    }
}