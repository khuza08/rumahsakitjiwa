package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.ConsultationSchedule;
import rumahsakitjiwa.model.Doctor;
import rumahsakitjiwa.model.Pasien;
import rumahsakitjiwa.model.Schedule;

public class ConsultationSchedulePanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable consultationTable;
    private JTextField txtScheduleId, txtConsultationDate, txtStartTime, txtEndTime, txtNotes, txtRoom;
    private JComboBox<String> cbStatus, cbPatient, cbDoctor;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnCheckAvailability;
    private int selectedScheduleId = -1;
    private Dashboard dashboard;
    private String userRole;

    public ConsultationSchedulePanel() {
        initComponents();
        setupTable();
        loadSchedules();

        // Tambahkan listener untuk combobox pasien dan dokter
        cbPatient.addActionListener(e -> updatePatientInfo());
        cbDoctor.addActionListener(e -> updateDoctorInfo());
    }

    private void loadPatientsToComboBox() {
        cbPatient.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT patient_code, full_name FROM patients ORDER BY patient_code";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String patientInfo = rs.getString("patient_code") + " - " + rs.getString("full_name");
                cbPatient.addItem(patientInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDoctorsToComboBox() {
        cbDoctor.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT doctor_code, full_name FROM doctors ORDER BY doctor_code";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String doctorInfo = rs.getString("doctor_code") + " - " + rs.getString("full_name");
                cbDoctor.addItem(doctorInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePatientInfo() {
        String selectedPatient = (String) cbPatient.getSelectedItem();
        if (selectedPatient != null && !selectedPatient.isEmpty()) {
            String[] parts = selectedPatient.split(" - ", 2);
            if (parts.length >= 2) {
                String patientCode = parts[0].trim();
                String patientName = parts[1].trim();
                // Tidak perlu mengupdate field karena kita menggunakan kode yang terambil dari combobox
            }
        }
    }

    private void updateDoctorInfo() {
        String selectedDoctor = (String) cbDoctor.getSelectedItem();
        if (selectedDoctor != null && !selectedDoctor.isEmpty()) {
            String[] parts = selectedDoctor.split(" - ", 2);
            if (parts.length >= 2) {
                String doctorCode = parts[0].trim();
                String doctorName = parts[1].trim();
                // Tidak perlu mengupdate field karena kita menggunakan kode yang terambil dari combobox
            }
        }
    }

    public void setUserRole(String role) {
        this.userRole = role;
        applyRolePermissions();
    }

    private void applyRolePermissions() {
        boolean isResepsionis = "resepsionis".equalsIgnoreCase(userRole);

        // Set combobox and field editability
        cbPatient.setEnabled(isResepsionis);
        cbDoctor.setEnabled(isResepsionis);
        txtConsultationDate.setEditable(isResepsionis);
        txtStartTime.setEditable(isResepsionis);
        txtEndTime.setEditable(isResepsionis);
        txtNotes.setEditable(isResepsionis);
        txtRoom.setEditable(isResepsionis);
        cbStatus.setEnabled(isResepsionis && "admin".equalsIgnoreCase(userRole)); // Only admin can change status

        // Set button visibility
        btnAdd.setVisible(isResepsionis);
        btnUpdate.setVisible(isResepsionis);
        btnDelete.setVisible("admin".equalsIgnoreCase(userRole));
        btnClear.setVisible(isResepsionis);
        btnCheckAvailability.setVisible(isResepsionis);
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

        JLabel titleLabel = new JLabel("Form Jadwal Konsultasi");
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
        formPanel.add(new JLabel("Pilih Pasien:"), gbc);
        cbPatient = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbPatient, gbc);

        // Load patients into the combobox
        loadPatientsToComboBox();

        // Doctor Selection
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Pilih Dokter:"), gbc);
        cbDoctor = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbDoctor, gbc);

        // Load doctors into the combobox
        loadDoctorsToComboBox();

        // Consultation Date
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tanggal Konsultasi:"), gbc);
        txtConsultationDate = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtConsultationDate, gbc);

        // Start Time
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Waktu Mulai:"), gbc);
        txtStartTime = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtStartTime, gbc);

        // End Time
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Waktu Selesai:"), gbc);
        txtEndTime = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtEndTime, gbc);

        // Room
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Ruangan:"), gbc);
        txtRoom = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtRoom, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Status:"), gbc);
        cbStatus = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled", "No-show"});
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbStatus, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Catatan:"), gbc);
        txtNotes = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtNotes, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        buttonPanel.setOpaque(false);

        btnCheckAvailability = createStyledButton("Cek Jadwal", new Color(0x9C27B0));
        btnAdd = createStyledButton("Jadwalkan", new Color(0x4CAF50));
        btnUpdate = createStyledButton("Update", new Color(0x2196F3));
        btnDelete = createStyledButton("Hapus", new Color(0xF44336));
        btnClear = createStyledButton("Clear", new Color(0x9E9E9E));

        btnCheckAvailability.addActionListener(e -> checkDoctorAvailability());
        btnAdd.addActionListener(e -> addSchedule());
        btnUpdate.addActionListener(e -> updateSchedule());
        btnDelete.addActionListener(e -> deleteSchedule());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnCheckAvailability);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 3;
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

        JLabel tableTitle = new JLabel("Daftar Jadwal Konsultasi");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(0x6da395));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "Kode Pasien", "Nama Pasien", "Kode Dokter", "Nama Dokter", "Tanggal", "Waktu", "Status", "Ruangan"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        consultationTable = new JTable(tableModel);
        consultationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        consultationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedSchedule();
            }
        });

        JTableHeader header = consultationTable.getTableHeader();
        header.setBackground(new Color(0x96A78D));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        consultationTable.setRowHeight(25);
        consultationTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        consultationTable.setGridColor(new Color(200, 200, 200));
        consultationTable.setSelectionBackground(new Color(0x6da395));

        // Hide ID column
        consultationTable.getColumnModel().getColumn(0).setMinWidth(0);
        consultationTable.getColumnModel().getColumn(0).setMaxWidth(0);
        consultationTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(consultationTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private void setupTable() {
        consultationTable.getColumnModel().getColumn(0).setMinWidth(0);
        consultationTable.getColumnModel().getColumn(0).setMaxWidth(0);
        consultationTable.getColumnModel().getColumn(0).setPreferredWidth(0);
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

    private void checkDoctorAvailability() {
        String selectedDoctor = (String) cbDoctor.getSelectedItem();
        String consultationDate = txtConsultationDate.getText().trim();

        if (selectedDoctor == null || selectedDoctor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih dokter terlebih dahulu!");
            cbDoctor.requestFocus();
            return;
        }

        if (consultationDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan tanggal konsultasi terlebih dahulu!");
            txtConsultationDate.requestFocus();
            return;
        }

        try {
            LocalDate date = LocalDate.parse(consultationDate);
            if (date.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Tanggal konsultasi tidak valid (harus hari ini atau di masa depan)!");
                return;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid! Gunakan format YYYY-MM-DD");
            return;
        }

        // Ekstrak kode dokter dari combobox
        String doctorCode = "";
        if (selectedDoctor != null && !selectedDoctor.isEmpty()) {
            String[] parts = selectedDoctor.split(" - ", 2);
            if (parts.length >= 2) {
                doctorCode = parts[0].trim();
            }
        }

        // Cek ketersediaan jadwal dokter
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ambil hari dalam seminggu dari tanggal konsultasi
            LocalDate consultationLocalDate = LocalDate.parse(consultationDate);
            String dayOfWeek = consultationLocalDate.getDayOfWeek().name();

            // Konversi ke nama hari Indonesia
            String indoDay = getIndonesianDay(dayOfWeek);

            // Cek apakah dokter memiliki jadwal di hari tersebut
            String sql = "SELECT s.shift FROM schedules s JOIN doctors d ON s.doctor_code = d.doctor_code WHERE d.doctor_code = ? AND FIND_IN_SET(?, s.days) > 0";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, doctorCode);
            pstmt.setString(2, indoDay);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String shift = rs.getString("shift");
                JOptionPane.showMessageDialog(this, "Dokter tersedia pada hari " + indoDay + " dengan shift " + shift + "\nSilakan atur waktu konsultasi sesuai shift ini.");
            } else {
                JOptionPane.showMessageDialog(this, "Dokter tidak memiliki jadwal pada hari " + indoDay + "!\nSilakan pilih tanggal lain.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saat memeriksa ketersediaan dokter: " + e.getMessage());
        }
    }

    private String getIndonesianDay(String englishDay) {
        switch (englishDay) {
            case "MONDAY": return "Senin";
            case "TUESDAY": return "Selasa";
            case "WEDNESDAY": return "Rabu";
            case "THURSDAY": return "Kamis";
            case "FRIDAY": return "Jumat";
            case "SATURDAY": return "Sabtu";
            case "SUNDAY": return "Minggu";
            default: return englishDay;
        }
    }

    private void addSchedule() {
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        if (!validateInput()) return;

        try {
            // Ekstrak kode dan nama pasien dari combobox
            String selectedPatient = (String) cbPatient.getSelectedItem();
            String patientCode = "";
            String patientName = "";
            if (selectedPatient != null && !selectedPatient.isEmpty()) {
                String[] parts = selectedPatient.split(" - ", 2);
                if (parts.length >= 2) {
                    patientCode = parts[0].trim();
                    patientName = parts[1].trim();
                }
            }

            // Ekstrak kode dan nama dokter dari combobox
            String selectedDoctor = (String) cbDoctor.getSelectedItem();
            String doctorCode = "";
            String doctorName = "";
            if (selectedDoctor != null && !selectedDoctor.isEmpty()) {
                String[] parts = selectedDoctor.split(" - ", 2);
                if (parts.length >= 2) {
                    doctorCode = parts[0].trim();
                    doctorName = parts[1].trim();
                }
            }

            // Validasi ketersediaan dokter untuk waktu yang dipilih
            if (!isDoctorAvailableForTime(doctorCode,
                                         LocalDate.parse(txtConsultationDate.getText().trim()),
                                         LocalTime.parse(txtStartTime.getText().trim()),
                                         LocalTime.parse(txtEndTime.getText().trim()))) {
                JOptionPane.showMessageDialog(this, "Dokter tidak tersedia pada waktu yang dipilih!");
                return;
            }

            // Periksa apakah ada konflik jadwal untuk pasien
            if (isPatientBusyAtTime(patientCode,
                                   LocalDate.parse(txtConsultationDate.getText().trim()),
                                   LocalTime.parse(txtStartTime.getText().trim()),
                                   LocalTime.parse(txtEndTime.getText().trim()))) {
                JOptionPane.showMessageDialog(this, "Pasien sudah memiliki jadwal lain pada waktu yang dipilih!");
                return;
            }

            ConsultationSchedule schedule = new ConsultationSchedule();
            schedule.setPatientCode(patientCode);
            schedule.setPatientName(patientName);
            schedule.setDoctorCode(doctorCode);
            schedule.setDoctorName(doctorName);
            schedule.setConsultationDate(java.sql.Timestamp.valueOf(
                LocalDate.parse(txtConsultationDate.getText().trim()).atTime(0, 0)));
            schedule.setStartTime(txtStartTime.getText().trim());
            schedule.setEndTime(txtEndTime.getText().trim());
            schedule.setStatus((String) cbStatus.getSelectedItem());
            schedule.setNotes(txtNotes.getText().trim());
            schedule.setRoom(txtRoom.getText().trim());

            if (insertSchedule(schedule)) {
                JOptionPane.showMessageDialog(this, "Jadwal konsultasi berhasil ditambahkan!");
                loadSchedules();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambah jadwal konsultasi!", "Error", JOptionPane.ERROR_MESSAGE);
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
            // Ekstrak kode dan nama pasien dari combobox
            String selectedPatient = (String) cbPatient.getSelectedItem();
            String patientCode = "";
            String patientName = "";
            if (selectedPatient != null && !selectedPatient.isEmpty()) {
                String[] parts = selectedPatient.split(" - ", 2);
                if (parts.length >= 2) {
                    patientCode = parts[0].trim();
                    patientName = parts[1].trim();
                }
            }

            // Ekstrak kode dan nama dokter dari combobox
            String selectedDoctor = (String) cbDoctor.getSelectedItem();
            String doctorCode = "";
            String doctorName = "";
            if (selectedDoctor != null && !selectedDoctor.isEmpty()) {
                String[] parts = selectedDoctor.split(" - ", 2);
                if (parts.length >= 2) {
                    doctorCode = parts[0].trim();
                    doctorName = parts[1].trim();
                }
            }

            // Validasi ketersediaan dokter untuk waktu yang dipilih
            if (!isDoctorAvailableForTime(doctorCode,
                                         LocalDate.parse(txtConsultationDate.getText().trim()),
                                         LocalTime.parse(txtStartTime.getText().trim()),
                                         LocalTime.parse(txtEndTime.getText().trim()))) {
                JOptionPane.showMessageDialog(this, "Dokter tidak tersedia pada waktu yang dipilih!");
                return;
            }

            // Periksa apakah ada konflik jadwal untuk pasien (kecuali jadwal yang sedang diupdate)
            if (isPatientBusyAtTime(patientCode,
                                   LocalDate.parse(txtConsultationDate.getText().trim()),
                                   LocalTime.parse(txtStartTime.getText().trim()),
                                   LocalTime.parse(txtEndTime.getText().trim()),
                                   selectedScheduleId)) {
                JOptionPane.showMessageDialog(this, "Pasien sudah memiliki jadwal lain pada waktu yang dipilih!");
                return;
            }

            ConsultationSchedule schedule = new ConsultationSchedule();
            schedule.setId(selectedScheduleId);
            schedule.setPatientCode(patientCode);
            schedule.setPatientName(patientName);
            schedule.setDoctorCode(doctorCode);
            schedule.setDoctorName(doctorName);
            schedule.setConsultationDate(java.sql.Timestamp.valueOf(
                LocalDate.parse(txtConsultationDate.getText().trim()).atTime(0, 0)));
            schedule.setStartTime(txtStartTime.getText().trim());
            schedule.setEndTime(txtEndTime.getText().trim());
            schedule.setStatus((String) cbStatus.getSelectedItem());
            schedule.setNotes(txtNotes.getText().trim());
            schedule.setRoom(txtRoom.getText().trim());

            if (updateScheduleInDB(schedule)) {
                JOptionPane.showMessageDialog(this, "Jadwal konsultasi berhasil diupdate!");
                loadSchedules();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengupdate jadwal konsultasi!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSchedule() {
        if (!"admin".equalsIgnoreCase(userRole)) return;

        if (selectedScheduleId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus jadwal konsultasi ini?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (deleteScheduleFromDB(selectedScheduleId)) {
                JOptionPane.showMessageDialog(this, "Jadwal konsultasi berhasil dihapus!");
                loadSchedules();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus jadwal konsultasi!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtScheduleId.setText("");
        cbPatient.setSelectedIndex(-1);
        cbDoctor.setSelectedIndex(-1);
        txtConsultationDate.setText("");
        txtStartTime.setText("");
        txtEndTime.setText("");
        txtRoom.setText("");
        cbStatus.setSelectedItem("Scheduled");
        txtNotes.setText("");
        selectedScheduleId = -1;
        consultationTable.clearSelection();
    }

    private boolean validateInput() {
        if (cbPatient.getSelectedItem() == null || cbPatient.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pasien harus dipilih!");
            cbPatient.requestFocus();
            return false;
        }

        if (cbDoctor.getSelectedItem() == null || cbDoctor.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dokter harus dipilih!");
            cbDoctor.requestFocus();
            return false;
        }

        try {
            LocalDate consultationDate = LocalDate.parse(txtConsultationDate.getText().trim());
            if (consultationDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Tanggal konsultasi tidak valid (harus hari ini atau di masa depan)!");
                return false;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid! Gunakan format YYYY-MM-DD");
            return false;
        }

        try {
            LocalTime startTime = LocalTime.parse(txtStartTime.getText().trim());
            LocalTime endTime = LocalTime.parse(txtEndTime.getText().trim());
            if (startTime.isAfter(endTime)) {
                JOptionPane.showMessageDialog(this, "Waktu mulai harus sebelum waktu selesai!");
                return false;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format waktu tidak valid! Gunakan format HH:MM");
            return false;
        }

        return true;
    }

    private boolean isDoctorAvailableForTime(String doctorCode, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Cek apakah dokter memiliki jadwal kerja di hari tersebut
            String dayOfWeek = getIndonesianDay(date.getDayOfWeek().name());
            
            String sql = "SELECT s.shift, st.start_time, st.end_time FROM schedules s " +
                        "JOIN doctors d ON s.doctor_code = d.doctor_code " +
                        "JOIN shift_times st ON s.shift = st.shift_name " +
                        "WHERE d.doctor_code = ? AND FIND_IN_SET(?, s.days) > 0";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, doctorCode);
            pstmt.setString(2, dayOfWeek);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                return false; // Dokter tidak kerja di hari tersebut
            }

            // Cek apakah waktu yang diminta berada dalam waktu shift dokter
            LocalTime docStartTime = rs.getTime("start_time").toLocalTime();
            LocalTime docEndTime = rs.getTime("end_time").toLocalTime();

            if (startTime.isBefore(docStartTime) || endTime.isAfter(docEndTime)) {
                return false; // Waktu konsultasi di luar waktu shift dokter
            }

            // Cek apakah ada jadwal konsultasi lain di waktu yang sama
            String checkConflictSql = "SELECT COUNT(*) FROM consultation_schedules " +
                                    "WHERE doctor_code = ? AND consultation_date = ? " +
                                    "AND status != 'Cancelled' AND status != 'Completed' " +
                                    "AND ((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?))";
            
            PreparedStatement checkPstmt = conn.prepareStatement(checkConflictSql);
            checkPstmt.setString(1, doctorCode);
            checkPstmt.setDate(2, java.sql.Date.valueOf(date));
            checkPstmt.setTime(3, java.sql.Time.valueOf(endTime));
            checkPstmt.setTime(4, java.sql.Time.valueOf(startTime));
            checkPstmt.setTime(5, java.sql.Time.valueOf(endTime));
            checkPstmt.setTime(6, java.sql.Time.valueOf(startTime));
            
            ResultSet conflictRs = checkPstmt.executeQuery();
            if (conflictRs.next() && conflictRs.getInt(1) > 0) {
                return false; // Ada konflik jadwal
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean isPatientBusyAtTime(String patientCode, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return isPatientBusyAtTime(patientCode, date, startTime, endTime, -1);
    }

    private boolean isPatientBusyAtTime(String patientCode, LocalDate date, LocalTime startTime, LocalTime endTime, int excludeScheduleId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            PreparedStatement pstmt;
            
            if (excludeScheduleId != -1) {
                sql = "SELECT COUNT(*) FROM consultation_schedules " +
                      "WHERE patient_code = ? AND consultation_date = ? AND id != ? " +
                      "AND status != 'Cancelled' AND status != 'Completed' " +
                      "AND ((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?))";
                
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, patientCode);
                pstmt.setDate(2, java.sql.Date.valueOf(date));
                pstmt.setInt(3, excludeScheduleId);
            } else {
                sql = "SELECT COUNT(*) FROM consultation_schedules " +
                      "WHERE patient_code = ? AND consultation_date = ? " +
                      "AND status != 'Cancelled' AND status != 'Completed' " +
                      "AND ((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?))";
                
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, patientCode);
                pstmt.setDate(2, java.sql.Date.valueOf(date));
            }
            
            pstmt.setTime(3, java.sql.Time.valueOf(endTime));
            pstmt.setTime(4, java.sql.Time.valueOf(startTime));
            pstmt.setTime(5, java.sql.Time.valueOf(endTime));
            pstmt.setTime(6, java.sql.Time.valueOf(startTime));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Ada konflik jadwal
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Asumsikan sibuk jika terjadi error
        }
        return false;
    }

    private void loadSelectedSchedule() {
        int selectedRow = consultationTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedScheduleId = (Integer) tableModel.getValueAt(selectedRow, 0);
            txtScheduleId.setText(String.valueOf(selectedScheduleId));

            // Set combobox pasien
            String patientCode = (String) tableModel.getValueAt(selectedRow, 1);
            String patientName = (String) tableModel.getValueAt(selectedRow, 2);
            String patientInfo = patientCode + " - " + patientName;
            cbPatient.setSelectedItem(patientInfo);

            // Set combobox dokter
            String doctorCode = (String) tableModel.getValueAt(selectedRow, 3);
            String doctorName = (String) tableModel.getValueAt(selectedRow, 4);
            String doctorInfo = doctorCode + " - " + doctorName;
            cbDoctor.setSelectedItem(doctorInfo);

            String dateStr = tableModel.getValueAt(selectedRow, 5).toString();
            txtConsultationDate.setText(dateStr.substring(0, 10));
            String timeStr = (String) tableModel.getValueAt(selectedRow, 6);
            String[] times = timeStr.split(" - ");
            if (times.length == 2) {
                txtStartTime.setText(times[0]);
                txtEndTime.setText(times[1]);
            }
            cbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 7));
            txtRoom.setText((String) tableModel.getValueAt(selectedRow, 8));
        }
    }

    private void loadSchedules() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM consultation_schedules ORDER BY consultation_date DESC, start_time";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("patient_code"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_code"),
                    rs.getString("doctor_name"),
                    rs.getDate("consultation_date"),
                    rs.getString("start_time") + " - " + rs.getString("end_time"),
                    rs.getString("status"),
                    rs.getString("room")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data jadwal: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean insertSchedule(ConsultationSchedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO consultation_schedules (patient_code, patient_name, doctor_code, doctor_name, consultation_date, start_time, end_time, status, notes, room, scheduled_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getPatientCode());
            pstmt.setString(2, schedule.getPatientName());
            pstmt.setString(3, schedule.getDoctorCode());
            pstmt.setString(4, schedule.getDoctorName());
            pstmt.setDate(5, new java.sql.Date(schedule.getConsultationDate().getTime()));
            pstmt.setString(6, schedule.getStartTime());
            pstmt.setString(7, schedule.getEndTime());
            pstmt.setString(8, schedule.getStatus());
            pstmt.setString(9, schedule.getNotes());
            pstmt.setString(10, schedule.getRoom());
            pstmt.setTimestamp(11, new java.sql.Timestamp(System.currentTimeMillis()));

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updateScheduleInDB(ConsultationSchedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE consultation_schedules SET patient_code=?, patient_name=?, doctor_code=?, doctor_name=?, consultation_date=?, start_time=?, end_time=?, status=?, notes=?, room=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getPatientCode());
            pstmt.setString(2, schedule.getPatientName());
            pstmt.setString(3, schedule.getDoctorCode());
            pstmt.setString(4, schedule.getDoctorName());
            pstmt.setDate(5, new java.sql.Date(schedule.getConsultationDate().getTime()));
            pstmt.setString(6, schedule.getStartTime());
            pstmt.setString(7, schedule.getEndTime());
            pstmt.setString(8, schedule.getStatus());
            pstmt.setString(9, schedule.getNotes());
            pstmt.setString(10, schedule.getRoom());
            pstmt.setInt(11, schedule.getId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean deleteScheduleFromDB(int scheduleId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM consultation_schedules WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, scheduleId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}