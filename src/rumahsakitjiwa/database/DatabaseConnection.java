package rumahsakitjiwa.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/rumahsakitjiwa";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    // Method untuk mendapatkan koneksi database
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver tidak ditemukan", e);
        }
    }

    // Method untuk membuat database dan tabel jika belum ada
    public static void initializeDatabase() {
        try {
            // Koneksi untuk membuat database
            String dbUrl = "jdbc:mysql://localhost:3306/";
            Connection conn = DriverManager.getConnection(dbUrl, USERNAME, PASSWORD);
            Statement stmt = conn.createStatement();
            
            // Buat database jika belum ada
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS rumah_sakit_jiwa");
            System.out.println("Database 'rumahsakitjiwa' siap digunakan.");
            
            conn.close();
            
            // Koneksi ke database yang sudah dibuat
            conn = getConnection();
            stmt = conn.createStatement();
            
            // Buat tabel rooms
            String createRoomsTable = """
                CREATE TABLE IF NOT EXISTS rooms (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    room_number VARCHAR(20) NOT NULL UNIQUE,
                    room_type VARCHAR(100) NOT NULL,
                    status ENUM('Tersedia', 'Terisi', 'Maintenance') DEFAULT 'Tersedia',
                    price DECIMAL(10,2) NOT NULL,
                    description TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
            
            stmt.executeUpdate(createRoomsTable);
            System.out.println("Tabel 'rooms' berhasil dibuat atau sudah ada.");
            
            // Buat tabel users jika belum ada
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    role ENUM('admin', 'staff', 'doctor') DEFAULT 'staff',
                    email VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
            
            stmt.executeUpdate(createUsersTable);
            System.out.println("Tabel 'users' berhasil dibuat atau sudah ada.");
            
            // Buat tabel patients
            String createPatientsTable = """
                CREATE TABLE IF NOT EXISTS patients (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    patient_code VARCHAR(20) NOT NULL UNIQUE,
                    full_name VARCHAR(100) NOT NULL,
                    birth_date DATE,
                    gender ENUM('Laki-laki', 'Perempuan') NOT NULL,
                    address TEXT,
                    phone VARCHAR(20),
                    emergency_contact VARCHAR(100),
                    emergency_phone VARCHAR(20),
                    medical_history TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
            
            stmt.executeUpdate(createPatientsTable);
            System.out.println("Tabel 'patients' berhasil dibuat atau sudah ada.");
            
            // Buat tabel doctors
            String createDoctorsTable = """
                CREATE TABLE IF NOT EXISTS doctors (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    doctor_code VARCHAR(20) NOT NULL UNIQUE,
                    full_name VARCHAR(100) NOT NULL,
                    specialization VARCHAR(100),
                    phone VARCHAR(20),
                    email VARCHAR(100),
                    schedule TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
            
            stmt.executeUpdate(createDoctorsTable);
            System.out.println("Tabel 'doctors' berhasil dibuat atau sudah ada.");
            
            // Insert default admin user jika belum ada
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
            var rs = stmt.executeQuery(checkAdmin);
            rs.next();
            if (rs.getInt(1) == 0) {
                String insertAdmin = """
                    INSERT INTO users (username, password, full_name, role, email) 
                    VALUES ('admin', 'admin123', 'Administrator', 'admin', 'admin@rumahsakitjiwa.com')
                    """;
                stmt.executeUpdate(insertAdmin);
                System.out.println("User admin default berhasil dibuat.");
            }
            
            // Insert sample room data jika tabel kosong
            String checkRooms = "SELECT COUNT(*) FROM rooms";
            rs = stmt.executeQuery(checkRooms);
            rs.next();
            if (rs.getInt(1) == 0) {
                String[] sampleRooms = {
                    "INSERT INTO rooms (room_number, room_type, status, price, description) VALUES ('101', 'VIP', 'Tersedia', 500000, 'Kamar VIP dengan fasilitas lengkap')",
                    "INSERT INTO rooms (room_number, room_type, status, price, description) VALUES ('102', 'VIP', 'Tersedia', 500000, 'Kamar VIP dengan fasilitas lengkap')",
                    "INSERT INTO rooms (room_number, room_type, status, price, description) VALUES ('201', 'Kelas 1', 'Tersedia', 300000, 'Kamar kelas 1 dengan AC dan TV')",
                    "INSERT INTO rooms (room_number, room_type, status, price, description) VALUES ('202', 'Kelas 1', 'Terisi', 300000, 'Kamar kelas 1 dengan AC dan TV')",
                    "INSERT INTO rooms (room_number, room_type, status, price, description) VALUES ('301', 'Kelas 2', 'Tersedia', 200000, 'Kamar kelas 2 standar')",
                    "INSERT INTO rooms (room_number, room_type, status, price, description) VALUES ('302', 'Kelas 2', 'Tersedia', 200000, 'Kamar kelas 2 standar')",
                    "INSERT INTO rooms (room_number, room_type, status, price, description) VALUES ('303', 'Kelas 2', 'Maintenance', 200000, 'Kamar kelas 2 sedang maintenance')"
                };
                
                for (String query : sampleRooms) {
                    stmt.executeUpdate(query);
                }
                System.out.println("Sample data kamar berhasil ditambahkan.");
            }
            
            conn.close();
            System.out.println("Inisialisasi database selesai!");
            
        } catch (SQLException e) {
            System.err.println("Error saat inisialisasi database: " + e.getMessage());
            e.printStackTrace();
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