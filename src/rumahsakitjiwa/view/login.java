package rumahsakitjiwa.view;
import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import rumahsakitjiwa.view.main;
import rumahsakitjiwa.database.DatabaseConnection;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.*;

public class login extends JFrame {
    public JTextField usernameField;
    public JPasswordField passwordField;
    public JButton loginButton;
    private Point mousePoint;
    private Rectangle normalBounds;
    private JSplitPane splitPane;
    private Rectangle preMinimizeBounds;
    private boolean isMaximized = false;
    
    // User session info
    public static String currentUserRole = "";
    public static String currentUsername = "";
    public static String currentFullName = "";

    public login() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 20);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            System.err.println("Gagal load FlatLaf");
        }

        setUndecorated(true);
        setTitle("Login - Rumah Sakit Jiwa");
        setSize(650, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(0);
        splitPane.setOpaque(false);
        add(splitPane, BorderLayout.CENTER);

        // ================= PANEL KIRI (Logo) =================
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0x1e3c35));
                g2d.fillRoundRect(0, 0, getWidth() + 15, getHeight(), 20, 20);
                g2d.fillRect(getWidth() - 10, 0, 15, getHeight());
                g2d.dispose();
            }
        };
        leftPanel.setOpaque(false);

        JPanel leftTopPanel = new JPanel(new BorderLayout());
        leftTopPanel.setOpaque(false);
        
        JPanel macOSButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        macOSButtons.setOpaque(false);
        
        JButton closeBtn = createMacOSButton(new Color(0xFF5F57));
        JButton minimizeBtn = createMacOSButton(new Color(0xFFBD2E));
        JButton maximizeBtn = createMacOSButton(new Color(0x28CA42));
        
        closeBtn.addActionListener(e -> { dispose(); System.exit(0); });
        minimizeBtn.addActionListener(e -> setState(JFrame.ICONIFIED));
        maximizeBtn.addActionListener(e -> toggleMaximize());

        macOSButtons.add(closeBtn);
        macOSButtons.add(minimizeBtn);
        macOSButtons.add(maximizeBtn);
        leftTopPanel.add(macOSButtons, BorderLayout.WEST);

        // Load logo dengan penanganan error
        JLabel logo;
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/rumahsakitjiwa/resource/medicare.png"));
            if (logoIcon.getImage() == null) {
                throw new NullPointerException("Logo image not found");
            }
            Image img = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            logo = new JLabel(new ImageIcon(img));
            logo.setHorizontalAlignment(SwingConstants.CENTER);
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
            // Fallback to text if logo not found
            logo = new JLabel("MEDICARE", SwingConstants.CENTER);
            logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
            logo.setForeground(Color.WHITE);
        }
        
        leftPanel.add(leftTopPanel, BorderLayout.NORTH);
        leftPanel.add(logo, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        // ================= PANEL KANAN (Form) =================
        JPanel rightPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0x6da395));
                g2d.fillRoundRect(-15, 0, getWidth() + 15, getHeight(), 20, 20);
                g2d.fillRect(0, 0, 15, getHeight());
                g2d.dispose();
            }
        };
        rightPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("LOGIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        rightPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel userLabel = new JLabel("👤");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        rightPanel.add(userLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));
        usernameField.setOpaque(false);
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setText("Username");
        usernameField.setForeground(new Color(200, 200, 200, 150));
        
        setupPlaceholder(usernameField, "Username");
        usernameField.addActionListener(e -> passwordField.requestFocus());
        
        gbc.gridx = 1;
        rightPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel passLabel = new JLabel("🔒");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        rightPanel.add(passLabel, gbc);
        
        passwordField = new JPasswordField(15);
        passwordField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));
        passwordField.setOpaque(false);
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setEchoChar((char) 0);
        passwordField.setText("Password");
        passwordField.setForeground(new Color(200, 200, 200, 150));
        
        setupPasswordPlaceholder(passwordField);
        passwordField.addActionListener(e -> loginButton.doClick());
        
        gbc.gridx = 1;
        rightPanel.add(passwordField, gbc);

        // Login Button
        loginButton = new JButton("LOGIN");
        loginButton.setBackground(new Color(0xD9E9CF));
        loginButton.setForeground(new Color(0x96A78D));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.addActionListener(e -> performLogin());
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        rightPanel.add(loginButton, gbc);

        // Database connection info
        gbc.gridy = 4;
        JLabel infoLabel = new JLabel("<html><div style='text-align:center;color:rgba(255,255,255,0.7);font-size:10px;'>" +
                "Silakan login menggunakan akun database Anda</div></html>");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(infoLabel, gbc);

        splitPane.setRightComponent(rightPanel);
        addUniversalDragFunctionality();
        normalBounds = getBounds();
    }

    private void setupPlaceholder(JTextField field, String placeholder) {
        field.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(200, 200, 200, 150));
                }
            }
        });
    }

    private void setupPasswordPlaceholder(JPasswordField field) {
        field.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals("Password")) {
                    field.setText("");
                    field.setForeground(Color.WHITE);
                    field.setEchoChar('●');
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText("Password");
                    field.setForeground(new Color(200, 200, 200, 150));
                }
            }
        });
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.equals("Username") || username.trim().isEmpty()) {
            showError("Please enter username");
            usernameField.requestFocus();
            return;
        }
        
        if (password.equals("Password") || password.trim().isEmpty()) {
            showError("Please enter password");
            passwordField.requestFocus();
            return;
        }
        
        UserInfo user = authenticateUser(username, password);
        if (user != null) {
            // Set session info
            currentUsername = user.username;
            currentUserRole = user.role;
            currentFullName = user.fullName;
            
            SwingUtilities.invokeLater(() -> {
                try {
                    main mainWindow = new main();
                    mainWindow.setVisible(true);
                    this.dispose();
                    
                    // Show welcome message
                    JOptionPane.showMessageDialog(mainWindow, 
                        "Selamat datang, " + currentFullName + "\nRole: " + currentUserRole, 
                        "Login Berhasil", 
                        JOptionPane.INFORMATION_MESSAGE);
                        
                } catch (Exception ex) {
                    showError("Error opening main window: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } else {
            showError("Username atau password salah!");
            clearPassword();
        }
    }

    // ================= DATABASE AUTHENTICATION =================
    private static class UserInfo {
        String username, role, fullName, email;
        UserInfo(String username, String role, String fullName, String email) {
            this.username = username;
            this.role = role;
            this.fullName = fullName;
            this.email = email;
        }
    }

    private UserInfo authenticateUser(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT username, role, full_name, email FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new UserInfo(
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("full_name"),
                    rs.getString("email")
                );
            }
        } catch (SQLException e) {
            System.err.println("Database authentication error: " + e.getMessage());
            showError("Koneksi database gagal. Silakan coba lagi.");
        }
        
        return null;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    private void clearPassword() {
        passwordField.setText("Password");
        passwordField.setForeground(new Color(200, 200, 200, 150));
        passwordField.setEchoChar((char) 0);
        usernameField.requestFocus();
    }

    private void toggleMaximize() {
        if (isMaximized) {
            setBounds(normalBounds);
            updateSplitPaneDivider(normalBounds.width);
            isMaximized = false;
            setExtendedState(JFrame.NORMAL);
        } else {
            normalBounds = getBounds();
            Rectangle targetBounds = getMaximizedScreenBounds();
            setBounds(targetBounds);
            updateSplitPaneDivider(targetBounds.width);
            isMaximized = true;
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        repaint();
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
                    mousePoint = SwingUtilities.convertPoint(comp, e.getPoint(), login.this);
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

    private Rectangle getMaximizedScreenBounds() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
        
        return new Rectangle(
            screenBounds.x + insets.left,
            screenBounds.y + insets.top,
            screenBounds.width - insets.left - insets.right,
            screenBounds.height - insets.top - insets.bottom
        );
    }

    private void updateSplitPaneDivider(int windowWidth) {
        if (splitPane != null) {
            int proportionalDivider = (int)(windowWidth * 0.385);
            splitPane.setDividerLocation(proportionalDivider);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new login().setVisible(true);
        });
    }
}