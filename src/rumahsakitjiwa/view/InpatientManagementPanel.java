package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.InpatientSchedule;
import rumahsakitjiwa.model.Pasien;
import rumahsakitjiwa.model.Doctor;
import rumahsakitjiwa.model.Room;

public class InpatientManagementPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable inpatientTable;
    private JTextField txtScheduleId, txtPatientCode, txtPatientName, txtDoctorCode, txtDoctorName;
    private JTextField txtRoomNumber, txtRoomType, txtAdmissionDate, txtDischargeDate, txtNotes, txtDiagnosis;
    private JComboBox<String> cbStatus;
    private JButton btnAdd, btnUpdate, btnCheckIn, btnCheckOut, btnClear, btnSearchPatient, btnSearchDoctor, btnSearchRoom;
    private int selectedScheduleId = -1;
    private Dashboard dashboard;
    private String userRole;

    public InpatientManagementPanel() {
        initComponents();
        setupTable();
        loadSchedules();
    }

    public void setUserRole(String role) {
        this.userRole = role;
        applyRolePermissions();
    }

    private void applyRolePermissions() {
        boolean isResepsionis = "resepsionis".equalsIgnoreCase(userRole);

        // Set field editability
        txtPatientCode.setEditable(isResepsionis);
        txtPatientName.setEditable(false); // Only readable
        txtDoctorCode.setEditable(isResepsionis);
        txtDoctorName.setEditable(false); // Only readable
        txtRoomNumber.setEditable(isResepsionis);
        txtRoomType.setEditable(false); // Only readable
        txtAdmissionDate.setEditable(isResepsionis);
        txtDischargeDate.setEditable("admin".equalsIgnoreCase(userRole)); // Only admin can edit discharge date
        txtNotes.setEditable(isResepsionis);
        txtDiagnosis.setEditable("admin".equalsIgnoreCase(userRole)); // Only admin can edit diagnosis
        cbStatus.setEnabled("admin".equalsIgnoreCase(userRole)); // Only admin can change status

        // Set button visibility
        btnAdd.setVisible(isResepsionis);
        btnUpdate.setVisible(isResepsionis);
        btnCheckIn.setVisible(isResepsionis);
        btnCheckOut.setVisible(isResepsionis);
        btnClear.setVisible(isResepsionis);
        btnSearchPatient.setVisible(isResepsionis);
        btnSearchDoctor.setVisible(isResepsionis);
        btnSearchRoom.setVisible(isResepsionis);
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = createFormPanel();
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
        formPanel.setPreferredSize(new Dimension(400, 0));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Form Manajemen Rawat Inap");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0x6da395));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Schedule ID
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("ID Jadwal:"), gbc);
        txtScheduleId = new JTextField(15);
        txtScheduleId.setEditable(false);
        txtScheduleId.setBackground(Color.LIGHT_GRAY);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtScheduleId, gbc);

        // Patient Code
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Kode Pasien:"), gbc);
        JPanel patientPanel = new JPanel(new BorderLayout(5, 0));
        txtPatientCode = new JTextField(10);
        btnSearchPatient = new JButton("Cari");
        patientPanel.add(txtPatientCode, BorderLayout.CENTER);
        patientPanel.add(btnSearchPatient, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(patientPanel, gbc);

        // Patient Name
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Nama Pasien:"), gbc);
        txtPatientName = new JTextField(15);
        txtPatientName.setEditable(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtPatientName, gbc);

        // Doctor Code
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Kode Dokter:"), gbc);
        JPanel doctorPanel = new JPanel(new BorderLayout(5, 0));
        txtDoctorCode = new JTextField(10);
        btnSearchDoctor = new JButton("Cari");
        doctorPanel.add(txtDoctorCode, BorderLayout.CENTER);
        doctorPanel.add(btnSearchDoctor, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(doctorPanel, gbc);

        // Doctor Name
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Nama Dokter:"), gbc);
        txtDoctorName = new JTextField(15);
        txtDoctorName.setEditable(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtDoctorName, gbc);

        // Room Number
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        formPanel.add(new JLabel("No. Kamar:"), gbc);
        JPanel roomPanel = new JPanel(new BorderLayout(5, 0));
        txtRoomNumber = new JTextField(10);
        btnSearchRoom = new JButton("Cari");
        roomPanel.add(txtRoomNumber, BorderLayout.CENTER);
        roomPanel.add(btnSearchRoom, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(roomPanel, gbc);

        // Room Type
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tipe Kamar:"), gbc);
        txtRoomType = new JTextField(15);
        txtRoomType.setEditable(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtRoomType, gbc);

        // Admission Date
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tanggal Masuk:"), gbc);
        txtAdmissionDate = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtAdmissionDate, gbc);

        // Discharge Date
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tanggal Keluar:"), gbc);
        txtDischargeDate = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtDischargeDate, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Status:"), gbc);
        cbStatus = new JComboBox<>(new String[]{"Scheduled", "Admitted", "Discharged", "Cancelled"});
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbStatus, gbc);

        // Diagnosis
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Diagnosis:"), gbc);
        txtDiagnosis = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtDiagnosis, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Catatan:"), gbc);
        txtNotes = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtNotes, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        buttonPanel.setOpaque(false);

        btnAdd = createStyledButton("Jadwalkan", new Color(0x4CAF50));
        btnUpdate = createStyledButton("Update", new Color(0x2196F3));
        btnCheckIn = createStyledButton("Check-in", new Color(0xFF9800));
        btnCheckOut = createStyledButton("Check-out", new Color(0x2196F3));
        btnClear = createStyledButton("Clear", new Color(0x9E9E9E));

        btnAdd.addActionListener(e -> addSchedule());
        btnUpdate.addActionListener(e -> updateSchedule());
        btnCheckIn.addActionListener(e -> checkInPatient());
        btnCheckOut.addActionListener(e -> checkOutPatient());
        btnClear.addActionListener(e -> clearForm());

        btnSearchPatient.addActionListener(e -> searchPatient());
        btnSearchDoctor.addActionListener(e -> searchDoctor());
        btnSearchRoom.addActionListener(e -> searchRoom());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnCheckIn);
        buttonPanel.add(btnCheckOut);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 13; gbc.gridwidth = 3;
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

        JLabel tableTitle = new JLabel("Daftar Pasien Rawat Inap");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(0x6da395));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "Kode Pasien", "Nama Pasien", "Kode Dokter", "Nama Dokter", "No. Kamar", "Tgl Masuk", "Tgl Keluar", "Status", "Diagnosis"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inpatientTable = new JTable(tableModel);
        inpatientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inpatientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedSchedule();
            }
        });

        JTableHeader header = inpatientTable.getTableHeader();
        header.setBackground(new Color(0x96A78D));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        inpatientTable.setRowHeight(25);
        inpatientTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        inpatientTable.setGridColor(new Color(200, 200, 200));
        inpatientTable.setSelectionBackground(new Color(0x6da395));

        // Hide ID column
        inpatientTable.getColumnModel().getColumn(0).setMinWidth(0);
        inpatientTable.getColumnModel().getColumn(0).setMaxWidth(0);
        inpatientTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(inpatientTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private void setupTable() {
        inpatientTable.getColumnModel().getColumn(0).setMinWidth(0);
        inpatientTable.getColumnModel().getColumn(0).setMaxWidth(0);
        inpatientTable.getColumnModel().getColumn(0).setPreferredWidth(0);
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

    private void searchPatient() {
        String patientCode = txtPatientCode.getText().trim();
        if (patientCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan kode pasien terlebih dahulu!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT full_name FROM patients WHERE patient_code = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, patientCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                txtPatientName.setText(rs.getString("full_name"));
            } else {
                JOptionPane.showMessageDialog(this, "Pasien dengan kode " + patientCode + " tidak ditemukan!");
                txtPatientName.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error mencari pasien: " + e.getMessage());
        }
    }

    private void searchDoctor() {
        String doctorCode = txtDoctorCode.getText().trim();
        if (doctorCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan kode dokter terlebih dahulu!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT full_name FROM doctors WHERE doctor_code = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, doctorCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                txtDoctorName.setText(rs.getString("full_name"));
            } else {
                JOptionPane.showMessageDialog(this, "Dokter dengan kode " + doctorCode + " tidak ditemukan!");
                txtDoctorName.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error mencari dokter: " + e.getMessage());
        }
    }

    private void searchRoom() {
        String roomNumber = txtRoomNumber.getText().trim();
        if (roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan nomor kamar terlebih dahulu!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT room_type FROM rooms WHERE room_number = ? AND status = 'Tersedia'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, roomNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                txtRoomType.setText(rs.getString("room_type"));
            } else {
                JOptionPane.showMessageDialog(this, "Kamar dengan nomor " + roomNumber + " tidak ditemukan atau tidak tersedia!");
                txtRoomType.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error mencari kamar: " + e.getMessage());
        }
    }

    private void addSchedule() {
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        if (!validateInput()) return;

        try {
            InpatientSchedule schedule = new InpatientSchedule();
            schedule.setPatientCode(txtPatientCode.getText().trim());
            schedule.setPatientName(txtPatientName.getText().trim());
            schedule.setDoctorCode(txtDoctorCode.getText().trim());
            schedule.setDoctorName(txtDoctorName.getText().trim());
            schedule.setRoomNumber(txtRoomNumber.getText().trim());
            schedule.setRoomType(txtRoomType.getText().trim());
            schedule.setAdmissionDate(java.sql.Timestamp.valueOf(
                LocalDate.parse(txtAdmissionDate.getText().trim()).atStartOfDay()));
            
            String dischargeDateStr = txtDischargeDate.getText().trim();
            if (!dischargeDateStr.isEmpty()) {
                schedule.setDischargeDate(java.sql.Timestamp.valueOf(
                    LocalDate.parse(dischargeDateStr).atStartOfDay()));
            }
            
            schedule.setStatus("Scheduled");
            schedule.setNotes(txtNotes.getText().trim());
            schedule.setDiagnosis(txtDiagnosis.getText().trim());

            if (insertSchedule(schedule)) {
                JOptionPane.showMessageDialog(this, "Jadwal rawat inap berhasil ditambahkan!");
                loadSchedules();
                updateRoomStatus(txtRoomNumber.getText().trim(), "Dipesan"); // Update status kamar
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambah jadwal rawat inap!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSchedule() {
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        if (selectedScheduleId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang akan diupdate!");
            return;
        }

        if (!validateInput()) return;

        try {
            InpatientSchedule schedule = new InpatientSchedule();
            schedule.setId(selectedScheduleId);
            schedule.setPatientCode(txtPatientCode.getText().trim());
            schedule.setPatientName(txtPatientName.getText().trim());
            schedule.setDoctorCode(txtDoctorCode.getText().trim());
            schedule.setDoctorName(txtDoctorName.getText().trim());
            schedule.setRoomNumber(txtRoomNumber.getText().trim());
            schedule.setRoomType(txtRoomType.getText().trim());
            schedule.setAdmissionDate(java.sql.Timestamp.valueOf(
                LocalDate.parse(txtAdmissionDate.getText().trim()).atStartOfDay()));
            
            String dischargeDateStr = txtDischargeDate.getText().trim();
            if (!dischargeDateStr.isEmpty()) {
                schedule.setDischargeDate(java.sql.Timestamp.valueOf(
                    LocalDate.parse(dischargeDateStr).atStartOfDay()));
            }
            
            schedule.setStatus((String) cbStatus.getSelectedItem());
            schedule.setNotes(txtNotes.getText().trim());
            schedule.setDiagnosis(txtDiagnosis.getText().trim());

            if (updateScheduleInDB(schedule)) {
                JOptionPane.showMessageDialog(this, "Jadwal rawat inap berhasil diupdate!");
                loadSchedules();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengupdate jadwal rawat inap!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkInPatient() {
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        if (selectedScheduleId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal pasien untuk check-in!");
            return;
        }

        try {
            // Update status ke Admitted dan kamar ke Terisi
            if (updateInpatientStatus(selectedScheduleId, "Admitted")) {
                // Update status kamar
                updateRoomStatus(txtRoomNumber.getText().trim(), "Terisi");
                
                JOptionPane.showMessageDialog(this, "Check-in pasien berhasil!");
                loadSchedules();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal melakukan check-in pasien!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkOutPatient() {
        if (!"admin".equalsIgnoreCase(userRole)) return;

        if (selectedScheduleId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal pasien untuk check-out!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin melakukan check-out pasien ini?\n" +
            "Proses ini akan menyelesaikan rawat inap dan membuka kamar.",
            "Konfirmasi Check-out", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Update status ke Discharged dan kamar ke Tersedia
                if (updateInpatientStatus(selectedScheduleId, "Discharged")) {
                    // Update status kamar
                    updateRoomStatus(txtRoomNumber.getText().trim(), "Tersedia");
                    
                    JOptionPane.showMessageDialog(this, "Check-out pasien berhasil!");
                    loadSchedules();
                    clearForm();
                    if (dashboard != null) dashboard.refreshDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal melakukan check-out pasien!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean updateInpatientStatus(int scheduleId, String status) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            if ("Discharged".equals(status)) {
                // Jika check-out, set tanggal discharge ke hari ini
                sql = "UPDATE inpatient_schedules SET status=?, discharge_date=? WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, status);
                pstmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
                pstmt.setInt(3, scheduleId);
                return pstmt.executeUpdate() > 0;
            } else {
                // Jika check-in, hanya update status
                sql = "UPDATE inpatient_schedules SET status=? WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, status);
                pstmt.setInt(2, scheduleId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateRoomStatus(String roomNumber, String status) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE rooms SET status=? WHERE room_number=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, roomNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void clearForm() {
        txtScheduleId.setText("");
        txtPatientCode.setText("");
        txtPatientName.setText("");
        txtDoctorCode.setText("");
        txtDoctorName.setText("");
        txtRoomNumber.setText("");
        txtRoomType.setText("");
        txtAdmissionDate.setText("");
        txtDischargeDate.setText("");
        cbStatus.setSelectedItem("Scheduled");
        txtDiagnosis.setText("");
        txtNotes.setText("");
        selectedScheduleId = -1;
        inpatientTable.clearSelection();
    }

    private boolean validateInput() {
        if (txtPatientCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode pasien harus diisi!");
            txtPatientCode.requestFocus();
            return false;
        }

        if (txtPatientName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama pasien harus diisi!");
            return false;
        }

        if (txtDoctorCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode dokter harus diisi!");
            txtDoctorCode.requestFocus();
            return false;
        }

        if (txtDoctorName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dokter harus diisi!");
            return false;
        }

        if (txtRoomNumber.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor kamar harus diisi!");
            txtRoomNumber.requestFocus();
            return false;
        }

        if (txtRoomType.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tipe kamar harus diisi!");
            return false;
        }

        try {
            LocalDate admissionDate = LocalDate.parse(txtAdmissionDate.getText().trim());
            if (admissionDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Tanggal masuk tidak valid (harus hari ini atau di masa depan)!");
                return false;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal masuk tidak valid! Gunakan format YYYY-MM-DD");
            return false;
        }

        String dischargeDateStr = txtDischargeDate.getText().trim();
        if (!dischargeDateStr.isEmpty()) {
            try {
                LocalDate dischargeDate = LocalDate.parse(dischargeDateStr);
                LocalDate admissionDate = LocalDate.parse(txtAdmissionDate.getText().trim());
                if (dischargeDate.isBefore(admissionDate)) {
                    JOptionPane.showMessageDialog(this, "Tanggal keluar tidak boleh sebelum tanggal masuk!");
                    return false;
                }
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Format tanggal keluar tidak valid! Gunakan format YYYY-MM-DD");
                return false;
            }
        }

        return true;
    }

    private void loadSelectedSchedule() {
        int selectedRow = inpatientTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedScheduleId = (Integer) tableModel.getValueAt(selectedRow, 0);
            txtScheduleId.setText(String.valueOf(selectedScheduleId));
            txtPatientCode.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtPatientName.setText((String) tableModel.getValueAt(selectedRow, 2));
            txtDoctorCode.setText((String) tableModel.getValueAt(selectedRow, 3));
            txtDoctorName.setText((String) tableModel.getValueAt(selectedRow, 4));
            txtRoomNumber.setText((String) tableModel.getValueAt(selectedRow, 5));
            txtAdmissionDate.setText(tableModel.getValueAt(selectedRow, 6).toString().substring(0, 10));
            
            Object dischargeDateObj = tableModel.getValueAt(selectedRow, 7);
            if (dischargeDateObj != null) {
                txtDischargeDate.setText(dischargeDateObj.toString().substring(0, 10));
            } else {
                txtDischargeDate.setText("");
            }
            
            cbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 8));
            txtDiagnosis.setText((String) tableModel.getValueAt(selectedRow, 9));
        }
    }

    private void loadSchedules() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM inpatient_schedules ORDER BY admission_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("patient_code"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_code"),
                    rs.getString("doctor_name"),
                    rs.getString("room_number"),
                    rs.getDate("admission_date"),
                    rs.getDate("discharge_date"),
                    rs.getString("status"),
                    rs.getString("diagnosis")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data rawat inap: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean insertSchedule(InpatientSchedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO inpatient_schedules (patient_code, patient_name, doctor_code, doctor_name, room_number, room_type, admission_date, discharge_date, status, diagnosis, notes, scheduled_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getPatientCode());
            pstmt.setString(2, schedule.getPatientName());
            pstmt.setString(3, schedule.getDoctorCode());
            pstmt.setString(4, schedule.getDoctorName());
            pstmt.setString(5, schedule.getRoomNumber());
            pstmt.setString(6, schedule.getRoomType());
            pstmt.setTimestamp(7, schedule.getAdmissionDate());
            pstmt.setTimestamp(8, schedule.getDischargeDate());
            pstmt.setString(9, schedule.getStatus());
            pstmt.setString(10, schedule.getDiagnosis());
            pstmt.setString(11, schedule.getNotes());
            pstmt.setTimestamp(12, new java.sql.Timestamp(System.currentTimeMillis()));

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updateScheduleInDB(InpatientSchedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE inpatient_schedules SET patient_code=?, patient_name=?, doctor_code=?, doctor_name=?, room_number=?, room_type=?, admission_date=?, discharge_date=?, status=?, diagnosis=?, notes=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getPatientCode());
            pstmt.setString(2, schedule.getPatientName());
            pstmt.setString(3, schedule.getDoctorCode());
            pstmt.setString(4, schedule.getDoctorName());
            pstmt.setString(5, schedule.getRoomNumber());
            pstmt.setString(6, schedule.getRoomType());
            pstmt.setTimestamp(7, schedule.getAdmissionDate());
            pstmt.setTimestamp(8, schedule.getDischargeDate());
            pstmt.setString(9, schedule.getStatus());
            pstmt.setString(10, schedule.getDiagnosis());
            pstmt.setString(11, schedule.getNotes());
            pstmt.setInt(12, schedule.getId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}