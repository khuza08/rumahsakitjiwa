package rumahsakitjiwa.view;

import javax.swing.*;
import java.awt.*;
import rumahsakitjiwa.database.DatabaseConnection;
import java.sql.*;

public class Dashboard extends JPanel {
    public Dashboard() {
        setLayout(new GridLayout(2, 3, 20, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        addDashboardStats();
    }

    private void addDashboardStats() {
        RoomStatistics roomStats = getRoomStatistics();
        int totalPatients = getTotalPatients();
        int activeDoctors = getActiveDoctors();

        add(createStatCard("Total Pasien", String.valueOf(totalPatients), new Color(0x4CAF50)));
        add(createStatCard("Pasien Hari Ini", "12", new Color(0x2196F3)));
        add(createStatCard("Dokter Aktif", String.valueOf(activeDoctors), new Color(0xFF9800)));
        add(createStatCard("Total Kamar", String.valueOf(roomStats.totalRooms), new Color(0x9C27B0)));
        add(createStatCard("Kamar Tersedia", String.valueOf(roomStats.availableRooms), new Color(0x00BCD4)));
        add(createStatCard("Jadwal Hari Ini", "18", new Color(0xF44336)));
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), 5, 15, 15);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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
        int totalRooms = 0;
        int availableRooms = 0;
        int occupiedRooms = 0;
        int maintenanceRooms = 0;
    }
    private RoomStatistics getRoomStatistics() {
        RoomStatistics stats = new RoomStatistics();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT status, COUNT(*) as count FROM rooms GROUP BY status";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                stats.totalRooms += count;

                switch (status) {
                    case "Tersedia":
                        stats.availableRooms = count;
                        break;
                    case "Terisi":
                        stats.occupiedRooms = count;
                        break;
                    case "Maintenance":
                        stats.maintenanceRooms = count;
                        break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting room statistics: " + e.getMessage());
            stats.totalRooms = 0;
            stats.availableRooms = 0;
            stats.occupiedRooms = 0;
            stats.maintenanceRooms = 0;
        }
        return stats;
    }

    private int getTotalPatients() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM patients";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient count: " + e.getMessage());
        }
        return 0;
    }

    private int getActiveDoctors() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM doctors WHERE is_active = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting doctor count: " + e.getMessage());
        }
        return 0;
    }
}
