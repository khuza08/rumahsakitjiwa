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
    private Timer animationTimer;
    private JSplitPane splitPane;
    private Rectangle preMinimizeBounds; 
    private boolean isMaximized = false;

    public login() {
        // ================= WAYLAND OPTIMIZATIONS =================
        // Set system properties untuk Wayland compatibility
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.opengl", "false"); // Disable OpenGL pada Wayland
        System.setProperty("sun.java2d.xrender", "false"); // Disable XRender
        System.setProperty("sun.java2d.pmoffscreen", "false"); // Disable pixmap caching
        
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 20);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("Component.arc", 15);
            
            // Additional Wayland-friendly settings
            UIManager.put("Component.focusWidth", 0);
            UIManager.put("Button.focusWidth", 0);
            UIManager.put("TextField.focusWidth", 0);
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
        
        // Set opaque background untuk menghindari transparency issues di Wayland
        setBackground(Color.WHITE);

        // Split layout kiri-kanan langsung
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(0);
        splitPane.setOpaque(true); // Set opaque untuk stability
        add(splitPane, BorderLayout.CENTER);

        // ================= PANEL KIRI (Logo) =================
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Wayland-friendly rendering hints
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                
                // Panel kiri dengan rounded di kiri atas dan kiri bawah
                g2d.setColor(new Color(0xB6CEB4));
                g2d.fillRoundRect(0, 0, getWidth() + 15, getHeight(), 20, 20);
                
                // Tutup bagian kanan agar persegi (sambung dengan panel kanan)
                g2d.fillRect(getWidth() - 10, 0, 15, getHeight());
                
                g2d.dispose();
            }
        };
        leftPanel.setOpaque(true);
        leftPanel.setBackground(new Color(0xB6CEB4));

        // Tombol macOS di panel kiri (atas kiri)
        JPanel leftTopPanel = new JPanel(new BorderLayout());
        leftTopPanel.setOpaque(true);
        leftTopPanel.setBackground(new Color(0xB6CEB4));
        
        JPanel macOSButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        macOSButtons.setOpaque(true);
        macOSButtons.setBackground(new Color(0xB6CEB4));
        
        JButton closeBtn = createMacOSButton(new Color(0xFF5F57));
        JButton minimizeBtn = createMacOSButton(new Color(0xFFBD2E));
        JButton maximizeBtn = createMacOSButton(new Color(0x28CA42));

        closeBtn.addActionListener(e -> animateClose());
        minimizeBtn.addActionListener(e -> animateMinimize());
        
        final JButton maxBtn = maximizeBtn;
        maxBtn.addActionListener(e -> {
            if (isMaximized) {
                animateRestore();
            } else {
                animateMaximize();
            }
        });

        macOSButtons.add(closeBtn);
        macOSButtons.add(minimizeBtn);
        macOSButtons.add(maximizeBtn);
        
        leftTopPanel.add(macOSButtons, BorderLayout.WEST);

        // Load logo
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/rumahsakitjiwa/resource/monologo.png"));
        Image img = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(img));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setOpaque(false);

        leftPanel.add(leftTopPanel, BorderLayout.NORTH);
        leftPanel.add(logo, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        // ================= PANEL KANAN (Form) =================
        JPanel rightPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Wayland-friendly rendering hints
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                
                // Panel kanan dengan rounded di kanan atas dan kanan bawah
                g2d.setColor(new Color(0x96A78D));
                g2d.fillRoundRect(-15, 0, getWidth() + 15, getHeight(), 20, 20);
                
                // Tutup bagian kiri agar persegi (sambung dengan panel kiri)
                g2d.fillRect(0, 0, 15, getHeight());
                
                g2d.dispose();
            }
        };
        rightPanel.setOpaque(true);
        rightPanel.setBackground(new Color(0x96A78D));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        ImageIcon userIcon = new ImageIcon(getClass().getResource("/rumahsakitjiwa/resource/user.png"));
        Image userImg = userIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        JLabel userLabel = new JLabel(new ImageIcon(userImg));
        userLabel.setOpaque(false);
        rightPanel.add(userLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));
        usernameField.setOpaque(false);
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        
        // Add placeholder text
        usernameField.setText("Username");
        usernameField.setForeground(new Color(200, 200, 200, 150)); 
        
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
        
        usernameField.addActionListener(e -> passwordField.requestFocus());
        
        gbc.gridx = 1;
        rightPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy++;
        ImageIcon lockIcon = new ImageIcon(getClass().getResource("/rumahsakitjiwa/resource/lock.png"));
        Image lockImg = lockIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        JLabel lockLabel = new JLabel(new ImageIcon(lockImg));
        lockLabel.setOpaque(false);
        rightPanel.add(lockLabel, gbc);
        
        passwordField = new JPasswordField(15);
        passwordField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));
        passwordField.setOpaque(false);
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setEchoChar((char) 0); 
        
        // Add placeholder text
        passwordField.setText("Password");
        passwordField.setForeground(new Color(200, 200, 200, 150)); 
        
        // Focus listeners for placeholder behavior
        passwordField.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(passwordField.getPassword()).equals("Password")) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.WHITE);
                    passwordField.setEchoChar('●'); 
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.setEchoChar((char) 0); 
                    passwordField.setText("Password");
                    passwordField.setForeground(new Color(200, 200, 200, 150));
                }
            }
        });
        
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
        loginButton.setOpaque(true); // Ensure button is opaque

        // ================= LOGIN ACTION LISTENER =================
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
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
            
            if (validateCredentials(username, password)) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        main mainWindow = new main();
                        mainWindow.setVisible(true);
                        this.dispose(); 
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
                passwordField.setText("Password");
                passwordField.setForeground(new Color(200, 200, 200, 150));
                passwordField.setEchoChar((char) 0);
                usernameField.requestFocus();
            }
        });

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        rightPanel.add(loginButton, gbc);

        splitPane.setRightComponent(rightPanel);

        // Drag functionality - simplified untuk Wayland
        addSimplifiedDragFunctionality();
        
        normalBounds = getBounds();
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowDeiconified(java.awt.event.WindowEvent e) {
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
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (!isMaximized && (animationTimer == null || !animationTimer.isRunning())) {
                    normalBounds = getBounds();
                }
            }
            
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                if (!isMaximized && (animationTimer == null || !animationTimer.isRunning())) {
                    normalBounds = getBounds();
                }
            }
        });
    }

    private boolean validateCredentials(String username, String password) {
        if (username.equals("admin") && password.equals("admin")) {
            return true;
        }
        
        if ((username.equals("dokter") && password.equals("dokter123")) ||
            (username.equals("perawat") && password.equals("perawat123"))) {
            return true;
        }
        
        return false;
    }

    // ================= SIMPLIFIED DRAG FOR WAYLAND =================
    private void addSimplifiedDragFunctionality() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mousePoint = e.getPoint();
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (!isMaximized) {
                    Point currentPoint = e.getLocationOnScreen();
                    // Simplified calculation untuk menghindari stuttering
                    int newX = currentPoint.x - mousePoint.x;
                    int newY = currentPoint.y - mousePoint.y;
                    setLocation(newX, newY);
                }
            }
        });
    }

    private JButton createMacOSButton(Color color) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(color);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                
                // Simplified hover effect untuk Wayland
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(0, 0, 0, 100)); // Reduced opacity
                    g2d.setStroke(new BasicStroke(1.0f));
                    
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    
                    if (color.equals(new Color(0xFF5F57))) { // Close
                        g2d.drawLine(centerX - 3, centerY - 3, centerX + 3, centerY + 3);
                        g2d.drawLine(centerX + 3, centerY - 3, centerX - 3, centerY + 3);
                    } else if (color.equals(new Color(0xFFBD2E))) { // Minimize
                        g2d.drawLine(centerX - 3, centerY, centerX + 3, centerY);
                    } else if (color.equals(new Color(0x28CA42))) { // Maximize/Restore
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
        
        // Simplified hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }

    // ================= OPTIMIZED ANIMATIONS FOR WAYLAND =================
    private void animateMinimize() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        preMinimizeBounds = getBounds();
        
        final Rectangle startBounds = getBounds();
        final int[] step = {0};
        final int maxSteps = 8; // Reduced frames untuk performance
        
        animationTimer = new Timer(20, e -> { // Slower interval untuk stability
            step[0]++;
            
            if (step[0] >= maxSteps) {
                animationTimer.stop();
                setState(JFrame.ICONIFIED);
                return;
            }
            
            float progress = step[0] / (float)maxSteps;
            progress = progress * progress; // Ease in
            
            int newWidth = Math.max(50, (int)(startBounds.width * (1 - progress * 0.8))); 
            int newHeight = Math.max(30, (int)(startBounds.height * (1 - progress * 0.8)));
            int newX = startBounds.x + (startBounds.width - newWidth) / 2;
            int newY = startBounds.y + (startBounds.height - newHeight) / 2;
            
            setBounds(newX, newY, newWidth, newHeight);
            repaint(); // Explicit repaint
        });
        animationTimer.start();
    }
    
    private void animateClose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        final Rectangle startBounds = getBounds();
        final Point center = new Point(
            startBounds.x + startBounds.width / 2,
            startBounds.y + startBounds.height / 2
        );
        
        final int[] step = {0};
        final int maxSteps = 10; // Reduced frames
        
        animationTimer = new Timer(25, e -> { // Slower for stability
            step[0]++;
            
            if (step[0] >= maxSteps) {
                animationTimer.stop();
                dispose();
                System.exit(0);
                return;
            }
            
            float scale = 1.0f - (step[0] / (float)maxSteps);
            int newWidth = Math.max(10, (int)(startBounds.width * scale));
            int newHeight = Math.max(10, (int)(startBounds.height * scale));
            
            setBounds(
                center.x - newWidth / 2,
                center.y - newHeight / 2,
                newWidth,
                newHeight
            );
            repaint(); // Explicit repaint
        });
        animationTimer.start();
    }
    
    private void animateMaximize() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        normalBounds = getBounds();
        
        final Rectangle startBounds = getBounds();
        final Rectangle targetBounds = getMaximizedScreenBounds();

        final int[] step = {0};
        final int maxSteps = 10; // Reduced frames
        
        animationTimer = new Timer(25, e -> { // Slower interval
            step[0]++;
            float progress = step[0] / (float) maxSteps;
            progress = 1 - (1 - progress) * (1 - progress); // ease out

            int newX = (int)(startBounds.x + (targetBounds.x - startBounds.x) * progress);
            int newY = (int)(startBounds.y + (targetBounds.y - startBounds.y) * progress);
            int newWidth = (int)(startBounds.width + (targetBounds.width - startBounds.width) * progress);
            int newHeight = (int)(startBounds.height + (targetBounds.height - startBounds.height) * progress);

            setBounds(newX, newY, newWidth, newHeight);
            updateSplitPaneDivider(newWidth);
            repaint(); // Explicit repaint

            if (step[0] >= maxSteps) {
                animationTimer.stop();
                setBounds(targetBounds);
                updateSplitPaneDivider(targetBounds.width);
                isMaximized = true;
                setExtendedState(JFrame.MAXIMIZED_BOTH);
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
        final Rectangle targetBounds = normalBounds != null ? normalBounds : new Rectangle(100, 100, 650, 350);

        final int[] step = {0};
        final int maxSteps = 10; // Reduced frames
        
        animationTimer = new Timer(25, e -> { // Slower interval
            step[0]++;
            float progress = step[0] / (float) maxSteps;
            progress = 1 - (1 - progress) * (1 - progress); // ease out

            int newX = (int)(startBounds.x + (targetBounds.x - startBounds.x) * progress);
            int newY = (int)(startBounds.y + (targetBounds.y - startBounds.y) * progress);
            int newWidth = (int)(startBounds.width + (targetBounds.width - startBounds.width) * progress);
            int newHeight = (int)(startBounds.height + (targetBounds.height - startBounds.height) * progress);

            setBounds(newX, newY, newWidth, newHeight);
            updateSplitPaneDivider(newWidth);
            repaint(); // Explicit repaint

            if (step[0] >= maxSteps) {
                animationTimer.stop();
                setBounds(targetBounds);
                updateSplitPaneDivider(targetBounds.width);
                isMaximized = false;
            }
        });

        animationTimer.start();
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