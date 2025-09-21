package rumahsakitjiwa.view;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public main() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            System.err.println("Failed to load FlatLaf");
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

        // Create header and sidebar panels
        JPanel headerPanel = createHeaderPanel();
        JPanel sidebarPanel = createSidebarPanel();

        // Create main content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Create dashboard and room CRUD panels
        dashboardPanel = new Dashboard();
        roomCRUDPanel = new RoomCRUDPanel();

        // Link dashboard instance to room panel for refresh
        roomCRUDPanel.setDashboard(dashboardPanel);

        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(createPatientPanel(), "PASIEN");
        contentPanel.add(createDoctorPanel(), "DOKTER");
        contentPanel.add(roomCRUDPanel, "KAMAR");
        contentPanel.add(createAppointmentPanel(), "JADWAL");
        contentPanel.add(createReportPanel(), "LAPORAN");

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

        JPanel macOSPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        macOSPanel.setOpaque(false);

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
                setBounds(normalBounds);
                setExtendedState(JFrame.NORMAL);
                isMaximized = false;
            } else {
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
                Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
                Rectangle targetBounds = new Rectangle(
                    screenBounds.x + insets.left,
                    screenBounds.y + insets.top,
                    screenBounds.width - insets.left - insets.right,
                    screenBounds.height - insets.top - insets.bottom);
                normalBounds = getBounds();
                setBounds(targetBounds);
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                isMaximized = true;
            }
        });

        macOSPanel.add(closeBtn);
        macOSPanel.add(minimizeBtn);
        macOSPanel.add(maximizeBtn);

        JLabel titleLabel = new JLabel("Sistem Informasi Rumah Sakit Jiwa", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridLayout(2,1,0,2));
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
        sidebarPanel.setPreferredSize(new Dimension(200,0));
        sidebarPanel.setOpaque(false);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        String[] menuItems = {"Dashboard","Data Pasien","Data Dokter","Data Kamar","Jadwal","Laporan"};
        String[] menuKeys = {"DASHBOARD","PASIEN","DOKTER","KAMAR","JADWAL","LAPORAN"};

        for(int i=0;i<menuItems.length;i++) {
            final String key=menuKeys[i];
            JButton btn = createMenuButton(menuItems[i]);
            btn.addActionListener(e -> cardLayout.show(contentPanel,key));
            sidebarPanel.add(btn);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0,8)));
        }

        sidebarPanel.add(Box.createVerticalGlue());

        JButton logoutBtn = createMenuButton("Logout");
        logoutBtn.setBackground(new Color(0xFF5F57));
        logoutBtn.addActionListener(e -> logout());
        sidebarPanel.add(logoutBtn);

        return sidebarPanel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        button.setPreferredSize(new Dimension(170,40));
        button.setBackground(new Color(255, 255, 255, 100));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.addMouseListener(new MouseAdapter(){
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
        panel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));

        JLabel descLabel = new JLabel("<html><div style='text-align:center;'>" +
                description.replace("\n","<br>")+"</div></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(255,255,255,200));

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
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            if(clockTimer != null) clockTimer.stop();
            SwingUtilities.invokeLater(() -> {
                new login().setVisible(true); // Pastikan class login ada
                this.dispose();
            });
        }
    }

    private JButton createMacOSButton(Color color) {
        JButton button = new JButton(){
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(0,0,getWidth(),getHeight());
                if(getModel().isRollover()) {
                    g2d.setColor(new Color(0,0,0,150));
                    g2d.setStroke(new BasicStroke(1.2f));
                    int centerX=getWidth()/2;
                    int centerY=getHeight()/2;
                    if(color.equals(new Color(0xFF5F57))) {
                        g2d.drawLine(centerX-3,centerY-3,centerX+3,centerY+3);
                        g2d.drawLine(centerX+3,centerY-3,centerX-3,centerY+3);
                    } else if(color.equals(new Color(0xFFBD2E))) {
                        g2d.drawLine(centerX-3,centerY,centerX+3,centerY);
                    } else if(color.equals(new Color(0x28CA42))) {
                        if(isMaximized) {
                            g2d.drawRect(centerX-2,centerY-1,3,3);
                            g2d.drawRect(centerX-1,centerY-2,3,3);
                        } else {
                            g2d.drawRect(centerX-2,centerY-2,4,4);
                        }
                    }
                }
                g2d.dispose();
            }
        };
        button.setPreferredSize(new Dimension(14,14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setPreferredSize(new Dimension(15,15));
                button.revalidate();
                button.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setPreferredSize(new Dimension(14,14));
                button.revalidate();
                button.repaint();
            }
        });
        return button;
    }

    private void addUniversalDragFunctionality() {
        addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e) {
                mousePoint = e.getPoint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseDragged(MouseEvent e) {
                if(!isMaximized){
                    Point currentPoint = e.getLocationOnScreen();
                    setLocation(currentPoint.x - mousePoint.x,currentPoint.y - mousePoint.y);
                }
            }
        });
        addDragToAllComponents(this);
    }

    private void addDragToAllComponents(Container container) {
        for(Component c : container.getComponents()){
            if(c instanceof JButton) continue;
            c.addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e) {
                    mousePoint = SwingUtilities.convertPoint(c,e.getPoint(),main.this);
                }
            });
            c.addMouseMotionListener(new MouseMotionAdapter(){
                public void mouseDragged(MouseEvent e) {
                    if(!isMaximized){
                        Point currentPoint = e.getLocationOnScreen();
                        setLocation(currentPoint.x - mousePoint.x,currentPoint.y - mousePoint.y);
                    }
                }
            });
            if(c instanceof Container){
                addDragToAllComponents((Container)c);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new main().setVisible(true));
    }
}
