package rumahsakitjiwa.view;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import rumahsakitjiwa.database.DatabaseConnection;

public class Dashboard extends JPanel {
    public Dashboard() {
        setLayout(new GridLayout(2, 3, 20, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        addDashboardStats();

        // Auto refresh dashboard every 10 seconds (10000 ms)
        Timer refreshTimer = new Timer(512, e -> refreshDashboard());
        refreshTimer.start();
    }

    public void refreshDashboard() {
        removeAll();
        addDashboardStats();
        revalidate();
        repaint();
    }

    private void addDashboardStats() {
        RoomStatistics roomStats = getRoomStatistics();
        int totalPatients = getTotalPatients();
        int patientsToday = getPatientsToday();
        int schedulesToday = getSchedulesToday(); // Perbaiki typo

        add(createStatCard("Total Pasien", String.valueOf(totalPatients), new Color(0x4CAF50)));
        add(createStatCard("Pasien Hari Ini", String.valueOf(patientsToday), new Color(0x2196F3)));
        add(createStatCard("Total Kamar", String.valueOf(roomStats.totalRooms), new Color(0x9C27B0)));
        add(createStatCard("Kamar Tersedia", String.valueOf(roomStats.availableRooms), new Color(0x00BCD4)));
        add(createStatCard("Jadwal Hari Ini", String.valueOf(schedulesToday), new Color(0xF44336)));
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255,255,255,220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), 5, 15, 15);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(100, 100, 100));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private static class RoomStatistics {
        int totalRooms=0;
        int availableRooms=0;
        int occupiedRooms=0;
        int maintenanceRooms=0;
    }

    private RoomStatistics getRoomStatistics() {
        RoomStatistics stats = new RoomStatistics();
        try(Connection conn = DatabaseConnection.getConnection()){
            String sql = "SELECT status, COUNT(*) AS count FROM rooms GROUP BY status";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                String status=rs.getString("status");
                int count=rs.getInt("count");
                stats.totalRooms += count;
                switch(status){
                    case "Tersedia": stats.availableRooms = count; break;
                    case "Terisi": stats.occupiedRooms = count; break;
                    case "Maintenance": stats.maintenanceRooms = count; break;
                }
            }
        }catch(SQLException e){
            System.err.println("Error getting room statistics: "+e.getMessage());
            stats.totalRooms=0; stats.availableRooms=0; stats.occupiedRooms=0; stats.maintenanceRooms=0;
        }
        return stats;
    }

    private int getTotalPatients() {
        try(Connection conn = DatabaseConnection.getConnection()){
            String sql = "SELECT COUNT(*) AS count FROM patients";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt("count");
            }
        }catch(SQLException e){
            System.err.println("Error getting patient count: "+e.getMessage());
        }
        return 0;
    }

    // New method to count patients registered today
    private int getPatientsToday() {
        try(Connection conn = DatabaseConnection.getConnection()){
            // Change 'created_at' to the actual date or timestamp column in your patients table
            String sql = "SELECT COUNT(*) AS count FROM patients WHERE DATE(created_at) = CURDATE()";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt("count");
            }
        }catch(SQLException e){
            System.err.println("Error getting today's patient count: "+e.getMessage());
        }
        return 0;
    }

    
    private int getSchedulesToday() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Mendapatkan nama hari ini dalam bahasa Indonesia
            String today = java.time.LocalDate.now()
                .getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("id", "ID"));
            
            String sql = "SELECT COUNT(*) AS count FROM schedules " +
                         "WHERE day_of_week = ? AND is_active = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, today);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's schedule count: " + e.getMessage());
        }
        return 0;
    }
}