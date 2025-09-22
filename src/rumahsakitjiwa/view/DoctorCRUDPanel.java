package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.Doctor;

public class DoctorCRUDPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable doctorTable;
    private JTextField txtDoctorCode, txtFullName, txtSpecialization, txtPhone, txtEmail;
    private JComboBox<String> cbActive;
    private JTextArea txtSchedule;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedDoctorId = -1;
    private Dashboard dashboard; // referensi ke dashboard
    
    // Komponen untuk filter
    private JComboBox<String> cbFilterSpecialization;
    private JComboBox<String> cbFilterStatus;

    public DoctorCRUDPanel() {
        initComponents();
        setupTable();
        loadDoctorData();
    }

    // Method untuk set referensi dashboard
    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form Panel (Kiri)
        JPanel formPanel = createFormPanel();

        // Table Panel (Kanan)
        JPanel tablePanel = createTablePanel();

        add(formPanel, BorderLayout.WEST);
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(350, 0));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        JLabel titleLabel = new JLabel("Form Data Dokter");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0x6da395));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        // Doctor Code
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Kode Dokter:"), gbc);
        txtDoctorCode = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtDoctorCode, gbc);

        // Full Name
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Nama Lengkap:"), gbc);
        txtFullName = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtFullName, gbc);

        // Specialization
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Spesialisasi:"), gbc);
        txtSpecialization = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtSpecialization, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("No. Telepon:"), gbc);
        txtPhone = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtPhone, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Email:"), gbc);
        txtEmail = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtEmail, gbc);

        // Active Status
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Status:"), gbc);
        cbActive = new JComboBox<>(new String[]{"Aktif", "Tidak Aktif"});
        gbc.gridx = 1;
        formPanel.add(cbActive, gbc);

        // Schedule
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Jadwal Praktek:"), gbc);
        txtSchedule = new JTextArea(3, 15);
        txtSchedule.setLineWrap(true);
        txtSchedule.setWrapStyleWord(true);
        txtSchedule.setText("Contoh: Senin-Jumat 08:00-16:00");
        txtSchedule.setForeground(Color.GRAY);
        
        // Add placeholder behavior
        txtSchedule.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSchedule.getText().equals("Contoh: Senin-Jumat 08:00-16:00")) {
                    txtSchedule.setText("");
                    txtSchedule.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSchedule.getText().trim().isEmpty()) {
                    txtSchedule.setText("Contoh: Senin-Jumat 08:00-16:00");
                    txtSchedule.setForeground(Color.GRAY);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(txtSchedule);
        gbc.gridx = 1;
        formPanel.add(scrollPane, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setOpaque(false);

        btnAdd = createStyledButton("Tambah", new Color(0x4CAF50));
        btnUpdate = createStyledButton("Update", new Color(0x2196F3));
        btnDelete = createStyledButton("Hapus", new Color(0xF44336));
        btnClear = createStyledButton("Clear", new Color(0x9E9E9E));

        btnAdd.addActionListener(e -> addDoctor());
        btnUpdate.addActionListener(e -> updateDoctor());
        btnDelete.addActionListener(e -> deleteDoctor());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel tableTitle = new JLabel("Daftar Dokter");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(0x6da395));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        // Create filter panel dengan tata letak yang lebih lebar
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(false);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Spesialisasi filter
        JLabel lblFilterSpecialization = new JLabel("Spesialisasi:");
        cbFilterSpecialization = new JComboBox<>();
        cbFilterSpecialization.addItem("Semua");
        loadSpecializations();
        
        // Status filter
        JLabel lblFilterStatus = new JLabel("Status:");
        cbFilterStatus = new JComboBox<>(new String[]{"Semua", "Aktif", "Tidak Aktif"});
        
        // Tambahkan listener untuk filter otomatis
        cbFilterSpecialization.addActionListener(e -> applyFilter());
        cbFilterStatus.addActionListener(e -> applyFilter());
        
        // Atur tata letak filter
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.1;
        filterPanel.add(lblFilterSpecialization, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.4;
        filterPanel.add(cbFilterSpecialization, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.weightx = 0.1;
        filterPanel.add(lblFilterStatus, gbc);
        
        gbc.gridx = 3; gbc.gridy = 0;
        gbc.weightx = 0.4;
        filterPanel.add(cbFilterStatus, gbc);
        
        // Setup table
        String[] columns = {"ID", "Kode", "Nama Lengkap", "Spesialisasi", "Telepon", "Email", "Status", "Jadwal"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        doctorTable = new JTable(tableModel);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedDoctor();
            }
        });

        // Style table
        JTableHeader header = doctorTable.getTableHeader();
        header.setBackground(new Color(0x96A78D));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        doctorTable.setRowHeight(25);
        doctorTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        doctorTable.setGridColor(new Color(200, 200, 200));
        doctorTable.setSelectionBackground(new Color(0x6da395));

        // Set column widths
        doctorTable.getColumnModel().getColumn(0).setMaxWidth(0); // Hide ID
        doctorTable.getColumnModel().getColumn(0).setMinWidth(0);
        doctorTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        // Add components to table panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        tablePanel.add(centerPanel, BorderLayout.CENTER);

        return tablePanel;
    }

    private void loadSpecializations() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT DISTINCT specialization FROM doctors ORDER BY specialization";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String specialization = rs.getString("specialization");
                if (specialization != null && !specialization.isEmpty()) {
                    cbFilterSpecialization.addItem(specialization);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data spesialisasi: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        String selectedSpecialization = (String) cbFilterSpecialization.getSelectedItem();
        String selectedStatus = (String) cbFilterStatus.getSelectedItem();
        
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT * FROM doctors WHERE 1=1");
            List<Object> parameters = new ArrayList<>();
            
            if (selectedSpecialization != null && !selectedSpecialization.equals("Semua")) {
                sql.append(" AND specialization = ?");
                parameters.add(selectedSpecialization);
            }
            
            if (selectedStatus != null && !selectedStatus.equals("Semua")) {
                sql.append(" AND is_active = ?");
                parameters.add(selectedStatus.equals("Aktif"));
            }
            
            sql.append(" ORDER BY doctor_code");
            
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("doctor_code"),
                    rs.getString("full_name"),
                    rs.getString("specialization"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getBoolean("is_active") ? "Aktif" : "Tidak Aktif",
                    rs.getString("schedule")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data dokter: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupTable() {
        // Hide ID column
        doctorTable.getColumnModel().getColumn(0).setMinWidth(0);
        doctorTable.getColumnModel().getColumn(0).setMaxWidth(0);
        doctorTable.getColumnModel().getColumn(0).setPreferredWidth(0);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(color.brighter());
                } else {
                    g2d.setColor(color);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());

                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + textHeight) / 2 - 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        button.setPreferredSize(new Dimension(80, 35));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));

        return button;
    }

    private void addDoctor() {
        if (validateInput()) {
            try {
                Doctor doctor = new Doctor();
                doctor.setDoctorCode(txtDoctorCode.getText().trim());
                doctor.setFullName(txtFullName.getText().trim());
                doctor.setSpecialization(txtSpecialization.getText().trim());
                doctor.setPhone(txtPhone.getText().trim());
                doctor.setEmail(txtEmail.getText().trim());
                doctor.setActive(cbActive.getSelectedIndex() == 0);
                
                String schedule = txtSchedule.getText().trim();
                if (schedule.equals("Contoh: Senin-Jumat 08:00-16:00")) {
                    schedule = "";
                }
                doctor.setSchedule(schedule);

                if (insertDoctor(doctor)) {
                    JOptionPane.showMessageDialog(this, "Data dokter berhasil ditambahkan!");
                    // Refresh specializations combo box
                    String currentSelection = (String) cbFilterSpecialization.getSelectedItem();
                    cbFilterSpecialization.removeAllItems();
                    cbFilterSpecialization.addItem("Semua");
                    loadSpecializations();
                    // Restore selection if possible
                    if (currentSelection != null && !currentSelection.equals("Semua")) {
                        cbFilterSpecialization.setSelectedItem(currentSelection);
                    } else {
                        cbFilterSpecialization.setSelectedIndex(0);
                    }
                    applyFilter(); // Apply current filter
                    clearForm();
                    // Refresh dashboard jika ada referensinya
                    if (dashboard != null) {
                        dashboard.refreshDashboard();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menambah data dokter!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateDoctor() {
        if (selectedDoctorId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dokter yang akan diupdate!");
            return;
        }

        if (validateInput()) {
            try {
                Doctor doctor = new Doctor();
                doctor.setId(selectedDoctorId);
                doctor.setDoctorCode(txtDoctorCode.getText().trim());
                doctor.setFullName(txtFullName.getText().trim());
                doctor.setSpecialization(txtSpecialization.getText().trim());
                doctor.setPhone(txtPhone.getText().trim());
                doctor.setEmail(txtEmail.getText().trim());
                doctor.setActive(cbActive.getSelectedIndex() == 0);
                
                String schedule = txtSchedule.getText().trim();
                if (schedule.equals("Contoh: Senin-Jumat 08:00-16:00")) {
                    schedule = "";
                }
                doctor.setSchedule(schedule);

                if (updateDoctorInDB(doctor)) {
                    JOptionPane.showMessageDialog(this, "Data dokter berhasil diupdate!");
                    if (dashboard != null) {
                    dashboard.refreshDashboard();  // Refresh dashboard here
                    }
                    // Refresh specializations combo box
                    String currentSelection = (String) cbFilterSpecialization.getSelectedItem();
                    cbFilterSpecialization.removeAllItems();
                    cbFilterSpecialization.addItem("Semua");
                    loadSpecializations();
                    // Restore selection if possible
                    if (currentSelection != null && !currentSelection.equals("Semua")) {
                        cbFilterSpecialization.setSelectedItem(currentSelection);
                    } else {
                        cbFilterSpecialization.setSelectedIndex(0);
                    }
                    applyFilter(); // Apply current filter
                    clearForm();
                    // Refresh dashboard jika ada referensinya
                    if (dashboard != null) {
                        dashboard.refreshDashboard();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengupdate data dokter!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteDoctor() {
        if (selectedDoctorId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dokter yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus data dokter ini?", 
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (deleteDoctorFromDB(selectedDoctorId)) {
                JOptionPane.showMessageDialog(this, "Data dokter berhasil dihapus!");
                // Refresh specializations combo box
                String currentSelection = (String) cbFilterSpecialization.getSelectedItem();
                cbFilterSpecialization.removeAllItems();
                cbFilterSpecialization.addItem("Semua");
                loadSpecializations();
                // Restore selection if possible
                if (currentSelection != null && !currentSelection.equals("Semua")) {
                    cbFilterSpecialization.setSelectedItem(currentSelection);
                } else {
                    cbFilterSpecialization.setSelectedIndex(0);
                }
                applyFilter(); // Apply current filter
                clearForm();
                // Refresh dashboard jika ada referensinya
                if (dashboard != null) {
                    dashboard.refreshDashboard();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data dokter!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtDoctorCode.setText("");
        txtFullName.setText("");
        txtSpecialization.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        cbActive.setSelectedIndex(0);
        txtSchedule.setText("Contoh: Senin-Jumat 08:00-16:00");
        txtSchedule.setForeground(Color.GRAY);
        selectedDoctorId = -1;
        doctorTable.clearSelection();
    }

    private boolean validateInput() {
        if (txtDoctorCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode Dokter tidak boleh kosong!");
            txtDoctorCode.requestFocus();
            return false;
        }
        if (txtFullName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Lengkap tidak boleh kosong!");
            txtFullName.requestFocus();
            return false;
        }
        if (txtSpecialization.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Spesialisasi tidak boleh kosong!");
            txtSpecialization.requestFocus();
            return false;
        }
        if (txtPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No. Telepon tidak boleh kosong!");
            txtPhone.requestFocus();
            return false;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email tidak boleh kosong!");
            txtEmail.requestFocus();
            return false;
        }
        
        // Validate email format
        String email = txtEmail.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Format email tidak valid!");
            txtEmail.requestFocus();
            return false;
        }
        
        return true;
    }

    private void loadSelectedDoctor() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedDoctorId = (Integer) tableModel.getValueAt(selectedRow, 0);
            txtDoctorCode.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtFullName.setText((String) tableModel.getValueAt(selectedRow, 2));
            txtSpecialization.setText((String) tableModel.getValueAt(selectedRow, 3));
            txtPhone.setText((String) tableModel.getValueAt(selectedRow, 4));
            txtEmail.setText((String) tableModel.getValueAt(selectedRow, 5));
            
            String status = (String) tableModel.getValueAt(selectedRow, 6);
            cbActive.setSelectedIndex(status.equals("Aktif") ? 0 : 1);
            
            String schedule = (String) tableModel.getValueAt(selectedRow, 7);
            if (schedule == null || schedule.isEmpty()) {
                txtSchedule.setText("Contoh: Senin-Jumat 08:00-16:00");
                txtSchedule.setForeground(Color.GRAY);
            } else {
                txtSchedule.setText(schedule);
                txtSchedule.setForeground(Color.BLACK);
            }
        }
    }

    private void loadDoctorData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM doctors ORDER BY doctor_code";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("doctor_code"),
                    rs.getString("full_name"),
                    rs.getString("specialization"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getBoolean("is_active") ? "Aktif" : "Tidak Aktif",
                    rs.getString("schedule")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data dokter: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean insertDoctor(Doctor doctor) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO doctors (doctor_code, full_name, specialization, phone, email, schedule, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, doctor.getDoctorCode());
            pstmt.setString(2, doctor.getFullName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getPhone());
            pstmt.setString(5, doctor.getEmail());
            pstmt.setString(6, doctor.getSchedule());
            pstmt.setBoolean(7, doctor.isActive());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updateDoctorInDB(Doctor doctor) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE doctors SET doctor_code=?, full_name=?, specialization=?, phone=?, email=?, schedule=?, is_active=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, doctor.getDoctorCode());
            pstmt.setString(2, doctor.getFullName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getPhone());
            pstmt.setString(5, doctor.getEmail());
            pstmt.setString(6, doctor.getSchedule());
            pstmt.setBoolean(7, doctor.isActive());
            pstmt.setInt(8, doctor.getId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean deleteDoctorFromDB(int doctorId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM doctors WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}