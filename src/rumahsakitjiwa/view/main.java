package rumahsakitjiwa.view;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class main extends JFrame {
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;
    private Timer animationTimer;
    private int animationStep;

    public main() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            System.err.println("Gagal load FlatLaf");
        }
        setUndecorated(true);
        setTitle("Main Window - Rumah Sakit Jiwa");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0)); // Transparan untuk rounded effect

        // Panel utama dengan warna dan rounded corner
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0x96A78D)); // warna utama
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);

        // Panel atas untuk tombol macOS
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        topPanel.setOpaque(false);

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

        topPanel.add(closeBtn);
        topPanel.add(minimizeBtn);
        topPanel.add(maximizeBtn);

        // Label welcome di tengah panel utama
        JLabel welcomeLabel = new JLabel("<html><div style='text-align:center;'>Selamat datang di aplikasi<br>Rumah Sakit Jiwa</div></html>", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(welcomeLabel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        addUniversalDragFunctionality();
        normalBounds = getBounds();
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

    // Gunakan method animate yang sudah diperbaiki agar animasi maksimal & restore berjalan mulus (dengan perbaikan step sebagai field kelas)

    private void animateClose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
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
        final Rectangle targetBounds = normalBounds != null ? normalBounds : new Rectangle(100, 100, 800, 600);
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
