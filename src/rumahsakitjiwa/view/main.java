package rumahsakitjiwa.view;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import rumahsakitjiwa.database.DatabaseConnection;

public class main extends JFrame {
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;
    private Timer animationTimer;
    private int animationStep;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private Timer clockTimer;

    // Panel utama untuk konten
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public main() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            System.err.println("Gagal load FlatLaf");
        }

        setUndecorated(true);
        setTitle("Dashboard - Rumah Sakit Jiwa");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        initializeComponents();
        startClock();
        addUniversalDragFunctionality();
        normalBounds = getBounds();
    }

    private void initializeComponents() {
        // Panel utama dengan rounded corner
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0x96A78D));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);

        // Header Panel dengan tombol macOS dan info
        JPanel headerPanel = createHeaderPanel();

        // Sidebar untuk navigasi
        JPanel sidebarPanel = createSidebarPanel();

        // Content panel dengan CardLayout untuk switching antar menu
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Tambahkan berbagai panel konten
        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createPatientPanel(), "PASIEN");
        contentPanel.add(createDoctorPanel(), "DOKTER");
        contentPanel.add(new RoomCRUDPanel(), "KAMAR"); // Pastikan RoomCRUDPanel ada di project Anda
        contentPanel.add(createAppointmentPanel(), "JADWAL");
        contentPanel.add(createReportPanel(), "LAPORAN");
        contentPanel.add(createSettingsPanel(), "PENGATURAN");

        // Layout utama
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(sidebarPanel, BorderLayout.WEST);
        centerPanel.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 10, 15));

        // Tombol macOS di kiri
        JPanel macOSPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        macOSPanel.setOpaque(false);

        JButton closeBtn = createMacOSButton(new Color(0xFF5F57));
        JButton minimizeBtn = createMacOSButton(new Color(0xFFBD2E));
        JButton maximizeBtn = createMacOSButton(new Color(0x28CA42));

        closeBtn.addActionListener(e -> animateClose());
        minimizeBtn.addActionListener(e -> setState(JFrame.ICONIFIED));
        maximizeBtn.addActionListener(e -> {
            if (isMaximized) {
                animateRestore();
            } else {
                animateMaximize();
            }
        });

        macOSPanel.add(closeBtn);
        macOSPanel.add(minimizeBtn);
        macOSPanel.add(maximizeBtn);

        // Title di tengah
        JLabel titleLabel = new JLabel("Sistem Informasi Rumah Sakit Jiwa", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        // Info waktu dan user di kanan
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);

        timeLabel = new JLabel("", SwingConstants.RIGHT);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timeLabel.setForeground(Color.WHITE);

        dateLabel = new JLabel("", SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLabel.setForeground(new Color(255, 255, 255, 200));

        infoPanel.add(timeLabel);
        infoPanel.add(dateLabel);

        headerPanel.add(macOSPanel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(infoPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setOpaque(false);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        String[] menuItems = {"Dashboard", "Data Pasien", "Data Dokter", "Data Kamar", "Jadwal", "Laporan", "Pengaturan"};
        String[] menuKeys = {"DASHBOARD", "PASIEN", "DOKTER", "KAMAR", "JADWAL", "LAPORAN", "PENGATURAN"};

        for (int i = 0; i < menuItems.length; i++) {
            final String menuKey = menuKeys[i];
            JButton menuBtn = createMenuButton(menuItems[i]);
            menuBtn.addActionListener(e -> cardLayout.show(contentPanel, menuKey));
            sidebarPanel.add(menuBtn);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // Spacer
        sidebarPanel.add(Box.createVerticalGlue());

        // Logout button
        JButton logoutBtn = createMenuButton("Logout");
        logoutBtn.setBackground(new Color(0xFF5F57));
        logoutBtn.addActionListener(e -> logout());
        sidebarPanel.add(logoutBtn);

        return sidebarPanel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(170, 40));
        button.setBackground(new Color(255, 255, 255, 100));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 255, 255, 150));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 255, 255, 100));
            }
        });

        return button;
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

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        RoomStatistics roomStats = getRoomStatistics();
        int totalPatients = getTotalPatients();
        int activeDoctors = getActiveDoctors();

        panel.add(createStatCard("Total Pasien", String.valueOf(totalPatients), new Color(0x4CAF50)));
        panel.add(createStatCard("Pasien Hari Ini", "12", new Color(0x2196F3))); // Bisa ditambah query untuk hari ini
        panel.add(createStatCard("Dokter Aktif", String.valueOf(activeDoctors), new Color(0xFF9800)));
        panel.add(createStatCard("Total Kamar", String.valueOf(roomStats.totalRooms), new Color(0x9C27B0)));
        panel.add(createStatCard("Kamar Tersedia", String.valueOf(roomStats.availableRooms), new Color(0x00BCD4)));
        panel.add(createStatCard("Jadwal Hari Ini", "18", new Color(0xF44336))); // Bisa ditambah query untuk hari ini

        return panel;
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

    private JPanel createPatientPanel() {
        return createContentPanel("Data Pasien",
                "Di sini akan ditampilkan daftar pasien, form tambah pasien baru,\n" +
                        "pencarian pasien, dan riwayat medis pasien.");
    }

    private JPanel createDoctorPanel() {
        return createContentPanel("Data Dokter",
                "Di sini akan ditampilkan daftar dokter, jadwal praktek,\n" +
                        "spesialisasi, dan informasi kontak dokter.");
    }

    private JPanel createAppointmentPanel() {
        return createContentPanel("Jadwal Konsultasi",
                "Di sini akan ditampilkan kalender jadwal, booking appointment,\n" +
                        "konfirmasi jadwal, dan reminder untuk pasien.");
    }

    private JPanel createReportPanel() {
        return createContentPanel("Laporan",
                "Di sini akan ditampilkan laporan statistik pasien,\n" +
                        "laporan keuangan, dan export data ke PDF/Excel.");
    }

    private JPanel createSettingsPanel() {
        return createContentPanel("Pengaturan",
                "Di sini akan ditampilkan pengaturan aplikasi,\n" +
                        "manajemen user, backup data, dan konfigurasi sistem.");
    }

    private JPanel createContentPanel(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel descLabel = new JLabel("<html><div style='text-align:center;'>" +
                description.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(255, 255, 255, 200));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descLabel, BorderLayout.CENTER);

        return panel;
    }

    private void startClock() {
        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        updateClock();
    }

    private void updateClock() {
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy");

        timeLabel.setText(timeFormat.format(now));
        dateLabel.setText(dateFormat.format(now));
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (clockTimer != null) {
                clockTimer.stop();
            }
            SwingUtilities.invokeLater(() -> {
                new login().setVisible(true);  // Pastikan class Login sudah ada
                this.dispose();
            });
        }
    }

    private JButton createMacOSButton(Color color) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.setStroke(new BasicStroke(1.2f));
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    if (color.equals(new Color(0xFF5F57))) {
                        g2d.drawLine(centerX - 3, centerY - 3, centerX + 3, centerY + 3);
                        g2d.drawLine(centerX + 3, centerY - 3, centerX - 3, centerY + 3);
                    } else if (color.equals(new Color(0xFFBD2E))) {
                        g2d.drawLine(centerX - 3, centerY, centerX + 3, centerY);
                    } else if (color.equals(new Color(0x28CA42))) {
                        if (isMaximized) {
                            g2d.drawRect(centerX - 2, centerY - 1, 3, 3);
                            g2d.drawRect(centerX - 1, centerY - 2, 3, 3);
                        } else {
                            g2d.drawRect(centerX - 2, centerY - 2, 4, 4);
                        }
                    }
                }
                g2d.dispose();
            }
        };
        button.setPreferredSize(new Dimension(14, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setPreferredSize(new Dimension(15, 15));
                button.revalidate();
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setPreferredSize(new Dimension(14, 14));
                button.revalidate();
                button.repaint();
            }
        });
        return button;
    }

    private void animateClose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        if (clockTimer != null) {
            clockTimer.stop();
        }
        final Rectangle startBounds = getBounds();
        final Point center = new Point(startBounds.x + startBounds.width / 2, startBounds.y + startBounds.height / 2);
        animationStep = 0;
        animationTimer = new Timer(16, e -> {
            animationStep++;
            if (animationStep > 20) {
                animationTimer.stop();
                dispose();
                System.exit(0);
                return;
            }
            float scale = 1.0f - (animationStep / 20.0f);
            int newWidth = (int) (startBounds.width * scale);
            int newHeight = (int) (startBounds.height * scale);
            if (newWidth > 0 && newHeight > 0) {
                setBounds(center.x - newWidth / 2, center.y - newHeight / 2, newWidth, newHeight);
            }
        });
        animationTimer.start();
    }

    private void animateMaximize() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        normalBounds = getBounds();
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
        final Rectangle finalTargetBounds = new Rectangle(
                screenBounds.x + insets.left,
                screenBounds.y + insets.top,
                screenBounds.width - insets.left - insets.right,
                screenBounds.height - insets.top - insets.bottom);
        final Rectangle startBounds = getBounds();
        animationStep = 0;
        final int maxSteps = 15;
        animationTimer = new Timer(16, e -> {
            animationStep++;
            float progress = animationStep / (float) maxSteps;
            progress = 1 - (1 - progress) * (1 - progress);
            int newX = (int) (startBounds.x + (finalTargetBounds.x - startBounds.x) * progress);
            int newY = (int) (startBounds.y + (finalTargetBounds.y - startBounds.y) * progress);
            int newWidth = (int) (startBounds.width + (finalTargetBounds.width - startBounds.width) * progress);
            int newHeight = (int) (startBounds.height + (finalTargetBounds.height - startBounds.height) * progress);
            setBounds(newX, newY, newWidth, newHeight);
            if (animationStep >= maxSteps) {
                animationTimer.stop();
                setBounds(finalTargetBounds);
                isMaximized = true;
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                repaint();
            }
        });
        animationTimer.start();
    }

    private void animateRestore() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        setExtendedState(JFrame.NORMAL);
        final Rectangle startBounds = getBounds();
        final Rectangle targetBounds = normalBounds != null ? normalBounds : new Rectangle(100, 100, 1200, 800);
        animationStep = 0;
        final int maxSteps = 15;
        animationTimer = new Timer(16, e -> {
            animationStep++;
            float progress = animationStep / (float) maxSteps;
            progress = 1 - (1 - progress) * (1 - progress);
            int newX = (int) (startBounds.x + (targetBounds.x - startBounds.x) * progress);
            int newY = (int) (startBounds.y + (targetBounds.y - startBounds.y) * progress);
            int newWidth = (int) (startBounds.width + (targetBounds.width - startBounds.width) * progress);
            int newHeight = (int) (startBounds.height + (targetBounds.height - startBounds.height) * progress);
            setBounds(newX, newY, newWidth, newHeight);
            if (animationStep >= maxSteps) {
                animationTimer.stop();
                setBounds(targetBounds);
                isMaximized = false;
                repaint();
            }
        });
        animationTimer.start();
    }

    private void addUniversalDragFunctionality() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mousePoint = e.getPoint();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (!isMaximized) {
                    Point currentPoint = e.getLocationOnScreen();
                    setLocation(currentPoint.x - mousePoint.x, currentPoint.y - mousePoint.y);
                }
            }
        });
        addDragToAllComponents(this);
    }

    private void addDragToAllComponents(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                continue;
            }
            comp.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    mousePoint = SwingUtilities.convertPoint(comp, e.getPoint(), main.this);
                }
            });
            comp.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (!isMaximized) {
                        Point currentPoint = e.getLocationOnScreen();
                        setLocation(currentPoint.x - mousePoint.x, currentPoint.y - mousePoint.y);
                    }
                }
            });
            if (comp instanceof Container) {
                addDragToAllComponents((Container) comp);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new main().setVisible(true));
    }
}
