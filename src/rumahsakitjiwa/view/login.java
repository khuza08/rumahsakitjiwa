package rumahsakitjiwa.view;
import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import rumahsakitjiwa.view.main; // Import main class
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
public class login extends JFrame {
    public JTextField usernameField;
    public JPasswordField passwordField;
    public JButton loginButton;
    private Point mousePoint;
    private Rectangle normalBounds;
    private JSplitPane splitPane;
    private Rectangle preMinimizeBounds; // Simpan bounds sebelum minimize
    private boolean isMaximized = false; // Track maximize state manual
    public login() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 20);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            System.err.println("Gagal load FlatLaf");
        }
        // Hapus title bar
        setUndecorated(true);
        
        setTitle("Login - Rumah Sakit Jiwa");
        setSize(650, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Buat background transparan untuk rounded effect
        setBackground(new Color(0, 0, 0, 0));
        // Split layout kiri-kanan langsung
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
                
                // Panel kiri dengan rounded di kiri atas dan kiri bawah
                g2d.setColor(new Color(0x1e3c35));
                g2d.fillRoundRect(0, 0, getWidth() + 15, getHeight(), 20, 20);
                
                // Tutup bagian kanan agar persegi (sambung dengan panel kanan)
                g2d.fillRect(getWidth() - 10, 0, 15, getHeight());
                
                g2d.dispose();
            }
        };
        leftPanel.setOpaque(false);
        // Tombol macOS di panel kiri (atas kiri)
        JPanel leftTopPanel = new JPanel(new BorderLayout());
        leftTopPanel.setOpaque(false);
        
        JPanel macOSButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        macOSButtons.setOpaque(false);
        
        JButton closeBtn = createMacOSButton(new Color(0xFF5F57));
        JButton minimizeBtn = createMacOSButton(new Color(0xFFBD2E));
        JButton maximizeBtn = createMacOSButton(new Color(0x28CA42));
        closeBtn.addActionListener(e -> {
            // Langsung dispose dan exit tanpa animasi
            dispose();
            System.exit(0);
        });
        minimizeBtn.addActionListener(e -> setState(JFrame.ICONIFIED));
        
        // Create final reference for maximize button
        final JButton maxBtn = maximizeBtn;
        maxBtn.addActionListener(e -> {
            if (isMaximized) {
                // Langsung restore tanpa animasi
                setBounds(normalBounds);
                updateSplitPaneDivider(normalBounds.width);
                isMaximized = false;
                setExtendedState(JFrame.NORMAL);
                repaint();
            } else {
                // Langsung maximize tanpa animasi
                normalBounds = getBounds();
                Rectangle targetBounds = getMaximizedScreenBounds();
                setBounds(targetBounds);
                updateSplitPaneDivider(targetBounds.width);
                isMaximized = true;
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                repaint();
            }
        });
        macOSButtons.add(closeBtn);
        macOSButtons.add(minimizeBtn);
        macOSButtons.add(maximizeBtn);
        
        leftTopPanel.add(macOSButtons, BorderLayout.WEST);
        // Load logo
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/rumahsakitjiwa/resource/medicare.png"));
        Image img = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(img));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(leftTopPanel, BorderLayout.NORTH);
        leftPanel.add(logo, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);
        // ================= PANEL KANAN (Form) =================
        JPanel rightPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Panel kanan dengan rounded di kanan atas dan kanan bawah
                g2d.setColor(new Color(0x6da395));
                g2d.fillRoundRect(-15, 0, getWidth() + 15, getHeight(), 20, 20);
                
                // Tutup bagian kiri agar persegi (sambung dengan panel kiri)
                g2d.fillRect(0, 0, 15, getHeight());
                
                g2d.dispose();
            }
        };
        rightPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        ImageIcon userIcon = new ImageIcon(getClass().getResource("/rumahsakitjiwa/resource/user.png"));
        Image userImg = userIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        rightPanel.add(new JLabel(new ImageIcon(userImg)), gbc);
        usernameField = new JTextField(15);
        usernameField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));
        usernameField.setOpaque(false);
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        
        // Add placeholder text
        usernameField.setText("Username");
        usernameField.setForeground(new Color(200, 200, 200, 150)); // Placeholder color
        
        // Focus listeners for placeholder behavior
        usernameField.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (usernameField.getText().equals("Username")) {
                    usernameField.setText("");
                    usernameField.setForeground(Color.WHITE);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setText("Username");
                    usernameField.setForeground(new Color(200, 200, 200, 150));
                }
            }
        });
        
        // Enter key listener - focus to password field
        usernameField.addActionListener(e -> passwordField.requestFocus());
        
        gbc.gridx = 1;
        rightPanel.add(usernameField, gbc);
        // Password
        gbc.gridx = 0; gbc.gridy++;
        ImageIcon lockIcon = new ImageIcon(getClass().getResource("/rumahsakitjiwa/resource/lock.png"));
        Image lockImg = lockIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        rightPanel.add(new JLabel(new ImageIcon(lockImg)), gbc);
        
        passwordField = new JPasswordField(15);
        passwordField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));
        passwordField.setOpaque(false);
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setEchoChar((char) 0); // Show placeholder text initially
        
        // Add placeholder text
        passwordField.setText("Password");
        passwordField.setForeground(new Color(200, 200, 200, 150)); // Placeholder color
        
        // Focus listeners for placeholder behavior
        passwordField.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(passwordField.getPassword()).equals("Password")) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.WHITE);
                    passwordField.setEchoChar('●'); // Set password masking
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.setEchoChar((char) 0); // Remove masking for placeholder
                    passwordField.setText("Password");
                    passwordField.setForeground(new Color(200, 200, 200, 150));
                }
            }
        });
        
        // Enter key listener - trigger login
        passwordField.addActionListener(e -> loginButton.doClick());
        
        gbc.gridx = 1;
        rightPanel.add(passwordField, gbc);
        // Tombol Login
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0xD9E9CF));
        loginButton.setForeground(new Color(0x96A78D));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // ================= LOGIN ACTION LISTENER =================
        loginButton.addActionListener(e -> {
            // Validasi input
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            // Cek apakah field kosong atau masih placeholder
            if (username.equals("Username") || username.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username", "Login Error", JOptionPane.ERROR_MESSAGE);
                usernameField.requestFocus();
                return;
            }
            
            if (password.equals("Password") || password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter password", "Login Error", JOptionPane.ERROR_MESSAGE);
                passwordField.requestFocus();
                return;
            }
            
            // Validasi credential
            if (validateCredentials(username, password)) {
                // Login berhasil - buka main window
                SwingUtilities.invokeLater(() -> {
                    try {
                        main mainWindow = new main();
                        mainWindow.setVisible(true);
                        this.dispose(); // Tutup login window
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, 
                            "Error opening main window: " + ex.getMessage(), 
                            "Application Error", 
                            JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                });
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                // Clear password field dan focus ke username
                passwordField.setText("Password");
                passwordField.setForeground(new Color(200, 200, 200, 150));
                passwordField.setEchoChar((char) 0);
                usernameField.requestFocus();
            }
        });
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        rightPanel.add(loginButton, gbc);
        splitPane.setRightComponent(rightPanel);
        // Drag functionality untuk seluruh window - bisa drag dari mana aja
        addUniversalDragFunctionality();
        
        // Save initial bounds untuk maximize/restore
        normalBounds = getBounds();
        
        // Add window listener untuk track perubahan bounds
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowDeiconified(java.awt.event.WindowEvent e) {
                // Restore ke bounds sebelum minimize
                if (preMinimizeBounds != null) {
                    setBounds(preMinimizeBounds);
                    if (splitPane != null) {
                        int proportionalDivider = (int)(preMinimizeBounds.width * 0.385);
                        splitPane.setDividerLocation(proportionalDivider);
                    }
                    preMinimizeBounds = null;
                }
            }
        });
        
        // Component listener untuk update normalBounds saat window di-resize/drag
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // Update normalBounds hanya jika tidak sedang maximize 
                if (!isMaximized) {
                    normalBounds = getBounds();
                }
            }
            
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                // Update normalBounds hanya jika tidak sedang maximize
                if (!isMaximized) {
                    normalBounds = getBounds();
                }
            }
        });
    }
    // ================= VALIDASI CREDENTIALS METHOD =================
    private boolean validateCredentials(String username, String password) {
        // Implementasi validasi sederhana - sesuaikan dengan kebutuhan
        
        // Contoh 1: Hardcode admin/admin untuk testing
        if (username.equals("admin") && password.equals("admin")) {
            return true;
        }
        
        // Contoh 2: Multiple users
        if ((username.equals("dokter") && password.equals("dokter123")) ||
            (username.equals("perawat") && password.equals("perawat123"))) {
            return true;
        }
        
        return false;
    }
    private void addUniversalDragFunctionality() {
        // Mouse listener untuk seluruh JFrame
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mousePoint = e.getPoint();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                // Disable drag jika sedang maximize
                if (!isMaximized) {
                    Point currentPoint = e.getLocationOnScreen();
                    setLocation(currentPoint.x - mousePoint.x, currentPoint.y - mousePoint.y);
                }
            }
        });
        // Juga tambahkan ke semua komponen child agar lebih responsif
        addDragToAllComponents(this);
    }
    private void addDragToAllComponents(Container container) {
        for (Component comp : container.getComponents()) {
            // Skip tombol macOS agar tetap bisa diklik
            if (comp instanceof JButton) {
                continue;
            }
            comp.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    mousePoint = SwingUtilities.convertPoint(comp, e.getPoint(), login.this);
                }
            });
            comp.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    // Disable drag jika sedang maximize
                    if (!isMaximized) {
                        Point currentPoint = e.getLocationOnScreen();
                        setLocation(currentPoint.x - mousePoint.x, currentPoint.y - mousePoint.y);
                    }
                }
            });
            // Recursively add to child components
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
                
                // Add symbols on hover for better UX
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.setStroke(new BasicStroke(1.2f));
                    
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    
                    if (color.equals(new Color(0xFF5F57))) { // Close - X
                        g2d.drawLine(centerX - 3, centerY - 3, centerX + 3, centerY + 3);
                        g2d.drawLine(centerX + 3, centerY - 3, centerX - 3, centerY + 3);
                    } else if (color.equals(new Color(0xFFBD2E))) { // Minimize - line
                        g2d.drawLine(centerX - 3, centerY, centerX + 3, centerY);
                    } else if (color.equals(new Color(0x28CA42))) { // Maximize/Restore
                        if (isMaximized) {
                            // Restore icon - two overlapping squares
                            g2d.drawRect(centerX - 2, centerY - 1, 3, 3);
                            g2d.drawRect(centerX - 1, centerY - 2, 3, 3);
                        } else {
                            // Maximize icon - single square
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
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setPreferredSize(new Dimension(15, 15));
                button.revalidate();
                button.repaint(); // Trigger repaint to show symbol
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setPreferredSize(new Dimension(14, 14));
                button.revalidate();
                button.repaint(); // Trigger repaint to hide symbol
            }
        });
        return button;
    }
    // Helper method untuk mendapatkan bounds maksimum
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
    // Helper method untuk update split pane divider secara proporsional
    private void updateSplitPaneDivider(int windowWidth) {
        if (splitPane != null) {
            int proportionalDivider = (int)(windowWidth * 0.385);
            splitPane.setDividerLocation(proportionalDivider);
        }
    }
    // ================= MAIN METHOD (UNTUK TESTING) =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new login().setVisible(true);
        });
    }
}
