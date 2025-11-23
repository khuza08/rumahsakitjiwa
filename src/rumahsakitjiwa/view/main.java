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
    private JLabel timeLabel;
    private JLabel dateLabel;
    private Timer clockTimer;
    
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    private Dashboard dashboardPanel;
    private RoomCRUDPanel roomCRUDPanel;
    private DoctorCRUDPanel doctorCRUDPanel;
    private PasienCRUDPanel pasienCRUDPanel;
    private ScheduleManagementPanel scheduleManagementPanel;
    private ReportPanel reportPanel;
    
    private String userRole;
    private String userName;
    
    public main() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            System.err.println("Gagal load FlatLaf");
        }
        
        setUndecorated(true);
        setTitle("Sistem Resepsionis - Rumah Sakit Jiwa");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        try {
            userRole = login.currentUserRole;
            userName = login.currentFullName;
        } catch (Exception e) {
            userRole = "admin";
            userName = "Administrator";
        }
        
        initializeComponents();
        startClock();
        addUniversalDragFunctionality();
        normalBounds = getBounds();
    }
    
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0x6da395));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);
        
        JPanel sidebarPanel = createSidebarPanel();
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        dashboardPanel = new Dashboard();
        contentPanel.add(dashboardPanel, "DASHBOARD");

        if ("resepsionis".equals(userRole)) {
            pasienCRUDPanel = new PasienCRUDPanel();
            pasienCRUDPanel.setUserRole(userRole);
            pasienCRUDPanel.setDashboard(dashboardPanel);
            contentPanel.add(pasienCRUDPanel, "PASIEN");
        }
        
        if ("admin".equals(userRole)) {
            roomCRUDPanel = new RoomCRUDPanel();
            doctorCRUDPanel = new DoctorCRUDPanel();
            scheduleManagementPanel = new ScheduleManagementPanel();
            reportPanel = new ReportPanel();
            
            roomCRUDPanel.setDashboard(dashboardPanel);
            
            contentPanel.add(doctorCRUDPanel, "DOKTER");
            contentPanel.add(roomCRUDPanel, "KAMAR");
            contentPanel.add(scheduleManagementPanel, "JADWAL");
            contentPanel.add(reportPanel, "LAPORAN");
            
        } else if ("resepsionis".equals(userRole)) {
            contentPanel.add(createContentPanel("Data Dokter", 
                "Akses terbatas. Hubungi administrator untuk manajemen dokter."), "DOKTER");
            contentPanel.add(createContentPanel("Data Kamar", 
                "Akses terbatas. Hubungi administrator untuk manajemen kamar."), "KAMAR");
            contentPanel.add(createContentPanel("Jadwal", 
                "Akses terbatas. Hubungi administrator untuk manajemen jadwal."), "JADWAL");
            contentPanel.add(createContentPanel("Laporan", 
                "Akses terbatas. Hubungi administrator untuk laporan."), "LAPORAN");
        }
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Buat header panel yang akan di atas content area (tidak menutupi sidebar)
        JPanel headerPanel = createHeaderPanel();
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(headerPanel, BorderLayout.NORTH);
        contentWrapper.add(contentPanel, BorderLayout.CENTER);
        
        centerPanel.add(sidebarPanel, BorderLayout.WEST);
        centerPanel.add(contentWrapper, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0x43786e));
                // Fill dari top ke bottom tanpa rounded corner
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setOpaque(false);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));
        
        // macOS Buttons Panel di paling atas
        JPanel macOSPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        macOSPanel.setOpaque(false);
        macOSPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JButton closeBtn = createMacOSButton(new Color(0xFF5F57));
        JButton minimizeBtn = createMacOSButton(new Color(0xFFBD2E));
        JButton maximizeBtn = createMacOSButton(new Color(0x28CA42));
        
        closeBtn.addActionListener(e -> {
            if (clockTimer != null) clockTimer.stop();
            dispose();
            System.exit(0);
        });
        
        minimizeBtn.addActionListener(e -> setState(JFrame.ICONIFIED));
        
        maximizeBtn.addActionListener(e -> {
            if (isMaximized) {
                setExtendedState(JFrame.NORMAL);
                if (normalBounds != null) {
                    setBounds(normalBounds);
                }
                isMaximized = false;
            } else {
                normalBounds = getBounds();
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                isMaximized = true;
            }
            repaint();
        });
        
        macOSPanel.add(closeBtn);
        macOSPanel.add(minimizeBtn);
        macOSPanel.add(maximizeBtn);
        
        sidebarPanel.add(macOSPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 30))); // Jarak setelah macOS buttons
        
        // Menu Items
        if ("admin".equals(userRole)) {
            String[] menuItems = {"Dashboard", "Data Dokter", "Data Kamar", "Jadwal"};
            String[] menuKeys = {"DASHBOARD", "DOKTER", "KAMAR", "JADWAL"};
            
            for (int i = 0; i < menuItems.length; i++) {
                final String menuKey = menuKeys[i];
                JButton menuBtn = createMenuButton(menuItems[i]);
                menuBtn.addActionListener(e -> cardLayout.show(contentPanel, menuKey));
                sidebarPanel.add(menuBtn);
                sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Jarak antar menu
            }
            
        } else if ("resepsionis".equals(userRole)) {
            String[] menuItems = {"Dashboard", "Data Pasien", "Laporan"};
            String[] menuKeys = {"DASHBOARD", "PASIEN", "LAPORAN"};
            
            for (int i = 0; i < menuItems.length; i++) {
                final String menuKey = menuKeys[i];
                JButton menuBtn = createMenuButton(menuItems[i]);
                menuBtn.addActionListener(e -> cardLayout.show(contentPanel, menuKey));
                sidebarPanel.add(menuBtn);
                sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Jarak antar menu
            }
        }
        
        sidebarPanel.add(Box.createVerticalGlue());
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Jarak sebelum logout button
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
                if (button.isEnabled())
                    button.setBackground(new Color(255, 255, 255, 150));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled())
                    button.setBackground(new Color(255, 255, 255, 100));
            }
        });
        
        return button;
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
                new login().setVisible(true);
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
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("Sistem Resepsionis - Rumah Sakit Jiwa", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        infoPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel(userName + " (" + userRole.toUpperCase() + ")", SwingConstants.RIGHT);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        userLabel.setForeground(new Color(255, 255, 255, 180));
        
        timeLabel = new JLabel("", SwingConstants.RIGHT);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timeLabel.setForeground(Color.WHITE);
        
        dateLabel = new JLabel("", SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLabel.setForeground(new Color(255, 255, 255, 200));
        
        infoPanel.add(userLabel);
        infoPanel.add(timeLabel);
        infoPanel.add(dateLabel);
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(infoPanel, BorderLayout.EAST);
        
        return headerPanel;
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
            if (comp instanceof JButton) continue;
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
    
    private static class RoomStatistics {
        int totalRooms;
        int availableRooms;
        int occupiedRooms;
        int maintenanceRooms;
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
                    case "Tersedia": stats.availableRooms = count; break;
                    case "Terisi": stats.occupiedRooms = count; break;
                    case "Maintenance": stats.maintenanceRooms = count; break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting room statistics: " + e.getMessage());
            stats.totalRooms = 0;
            stats.availableRooms = 0;
        }
        return stats;
    }
    
    private int getTotalPatients() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM patients";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("count");
        } catch (SQLException e) {
            System.err.println("Error getting patient count: " + e.getMessage());
        }
        return 0;
    }
   
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new main().setVisible(true));
    }
}