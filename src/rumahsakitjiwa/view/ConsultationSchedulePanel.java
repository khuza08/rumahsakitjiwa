package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import com.toedter.calendar.JDateChooser;
import com.github.lgooddatepicker.components.TimePicker;
import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.ConsultationSchedule;
import rumahsakitjiwa.controller.ConsultationScheduleController;
import rumahsakitjiwa.dao.ConsultationScheduleDataAccess;
import rumahsakitjiwa.utils.ConsultationScheduleHelper;

public class ConsultationSchedulePanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable consultationTable;
    private JTextField txtScheduleId;
    private JDateChooser dateChooser;  // For consultation date
    private TimePicker tpStartTime, tpEndTime;  // Use LGoodDatePicker TimePicker for time input
    private JComboBox<String> cbStatus, cbPatient, cbDoctor, cbInpatientRequired, cbRoomType, cbRoom;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnCheckAvailability;
    private int selectedScheduleId = -1;
    private Dashboard dashboard;
    private String userRole;

    // Menambahkan controller dan DAO sebagai properti kelas
    private ConsultationScheduleController controller;
    private ConsultationScheduleDataAccess dataAccess;

    // Utility method to get time from LGoodDatePicker TimePicker
    private String formatTimeFromTimePicker(TimePicker timePicker) {
        java.time.LocalTime selectedTime = timePicker.getTime();
        if (selectedTime != null) {
            return selectedTime.toString().substring(0, 5); // Return in HH:mm format (first 5 chars of HH:mm:ss)
        }
        return "00:00"; // Default time if null
    }

    public ConsultationSchedulePanel() {
        this.controller = new ConsultationScheduleController();
        this.dataAccess = new ConsultationScheduleDataAccess();
        initComponents();
        setupTable();
        loadSchedules();
        loadRoomTypes();  // Load room types after initialization
        // Don't load rooms initially - only load when room type is selected
        cbRoomType.setSelectedIndex(-1); // Set room type to null initially
        cbRoom.setSelectedIndex(-1);     // Set room to null initially

        // Don't load doctors initially - will be loaded when date is selected

        // Tambahkan listener untuk combobox pasien dan dokter
        cbPatient.addActionListener(e -> updatePatientInfo());
        cbDoctor.addActionListener(e -> updateDoctorInfo());
        // Add listener for room type to filter room numbers
        cbRoomType.addActionListener(e -> updateRoomsForType());
        // Add listener for room selection to update room type
        cbRoom.addActionListener(e -> updateRoomInfo());
        // Add listener for date chooser to filter doctors based on selected day
        dateChooser.getDateEditor().addPropertyChangeListener(e -> {
            if ("date".equals(e.getPropertyName())) {
                updateDoctorsForSelectedDate();
            }
        });
    }

    private void updateDoctorsForSelectedDate() {
        // If no date is selected, keep the combobox empty
        if (dateChooser.getDate() == null) {
            cbDoctor.removeAllItems();
            return;
        }

        // Get the selected date and determine the day of week
        java.util.Date selectedDate = dateChooser.getDate();
        java.time.LocalDate localDate = selectedDate.toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate();

        // Get the day of week in English (e.g., "MONDAY", "TUESDAY", etc.)
        String dayOfWeek = localDate.getDayOfWeek().name();

        // Convert to Indonesian day name
        String indoDay = getIndonesianDay(dayOfWeek);

        cbDoctor.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to find doctors who have schedules on the selected day
            String sql = "SELECT d.doctor_code, d.full_name FROM doctors d " +
                        "JOIN schedules s ON d.doctor_code = s.doctor_code " +
                        "WHERE FIND_IN_SET(?, s.days) > 0 AND s.is_active = 1 AND d.status = 'Aktif'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, indoDay);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String doctorInfo = rs.getString("doctor_code") + " - " + rs.getString("full_name");
                cbDoctor.addItem(doctorInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            default: return englishDay; // Return original if not found
        }
    }

    private void updateRoomsForType() {
        String selectedRoomType = (String) cbRoomType.getSelectedItem();
        cbRoom.removeAllItems();
        if (selectedRoomType == null || selectedRoomType.isEmpty()) {
            // When no room type is selected, keep room combobox empty
            return;
        } else {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT room_number, room_type FROM rooms WHERE room_type = ? AND status = 'Tersedia' ORDER BY room_number";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, selectedRoomType);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String roomInfo = rs.getString("room_number") + " (" + rs.getString("room_type") + ")";
                    cbRoom.addItem(roomInfo);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateRoomInfo() {
        String selectedRoom = (String) cbRoom.getSelectedItem();
        if (selectedRoom != null && !selectedRoom.isEmpty()) {
            // Ekstrak room type dari format "room_number (room_type)"
            int openParenIndex = selectedRoom.indexOf('(');
            if (openParenIndex > 0) {
                // Ekstrak room type dari dalam tanda kurung
                String roomType = selectedRoom.substring(openParenIndex + 1, selectedRoom.length() - 1);
                cbRoomType.setSelectedItem(roomType);
            }
        }
    }

    private void loadDoctorsToComboBox() {
        cbDoctor.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT doctor_code, full_name FROM doctors WHERE status = 'Aktif' ORDER BY doctor_code";
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
        dateChooser.setEnabled(isResepsionis);
        tpStartTime.setEnabled(isResepsionis);
        tpEndTime.setEnabled(isResepsionis);
        cbRoomType.setEnabled(isResepsionis);
        cbRoom.setEnabled(isResepsionis);
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
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("ID Jadwal:"), gbc);
        txtScheduleId = new JTextField(15);
        txtScheduleId.setEditable(false);
        txtScheduleId.setBackground(Color.LIGHT_GRAY);
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(txtScheduleId, gbc);

        // Patient Code
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Pilih Pasien:"), gbc);
        cbPatient = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(cbPatient, gbc);

        // Load patients into the combobox
        loadPatientsToComboBox();

        // Doctor Selection
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Pilih Dokter:"), gbc);
        cbDoctor = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(cbDoctor, gbc);

        // Don't load doctors initially - only load when date is selected
        cbDoctor.removeAllItems();

        // Consultation Date
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Tanggal Konsultasi:"), gbc);
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(dateChooser, gbc);

        // Start Time
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Waktu Mulai:"), gbc);
        tpStartTime = new TimePicker(); // Default TimePicker
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(tpStartTime, gbc);

        // End Time
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Waktu Selesai:"), gbc);
        tpEndTime = new TimePicker(); // Default TimePicker
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(tpEndTime, gbc);

        // Room Type
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Tipe Kamar:"), gbc);
        cbRoomType = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(cbRoomType, gbc);

        // Room Selection
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Pilih Kamar:"), gbc);
        cbRoom = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(cbRoom, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Status:"), gbc);
        cbStatus = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled", "No-show"});
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(cbStatus, gbc);

        // Inpatient Required
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Perlu Rawat Inap:"), gbc);
        cbInpatientRequired = new JComboBox<>(new String[]{"Tidak", "Ya"});
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(cbInpatientRequired, gbc);

        // No inpatient details panel - removed as requested

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        buttonPanel.setOpaque(false);

        btnCheckAvailability = createStyledButton("Cek Jadwal", new Color(0x9C27B0));
        btnAdd = createStyledButton("Jadwalkan", new Color(0x4CAF50));
        btnUpdate = createStyledButton("Update", new Color(0x2196F3));

        // Tombol booking kamar, ditempatkan sejajar dengan tombol update
        JButton btnCreateBooking = createStyledButton("Booking", new Color(0xFF9800)); // Warna oranye agar terlihat berbeda
        btnCreateBooking.addActionListener(e -> createBookingFromConsultation());

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
        buttonPanel.add(btnCreateBooking); // Menempatkan tombol booking di sebelah update
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 3; // Mengganti indeks baris karena panel inap sekarang di indeks 11
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

        String[] columns = {"ID", "Kode Pasien", "Nama Pasien", "Kode Dokter", "Nama Dokter", "Tanggal", "Waktu", "Status", "Ruangan", "Rawat Inap?", "Tipe Kamar Rekom.", "Durasi Rekom.", "Catatan Rawat"};
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

        if (selectedDoctor == null || selectedDoctor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih dokter terlebih dahulu!");
            cbDoctor.requestFocus();
            return;
        }

        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Masukkan tanggal konsultasi terlebih dahulu!");
            dateChooser.requestFocus();
            return;
        }

        String consultationDate;
        try {
            // Convert Date to LocalDate
            java.util.Date selectedDate = dateChooser.getDate();
            LocalDate localDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            consultationDate = localDate.toString(); // Format as YYYY-MM-DD

            if (localDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Tanggal konsultasi tidak valid (harus hari ini atau di masa depan)!");
                return;
            }
        } catch (Exception e) {
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

        // Get date from date chooser
        LocalDate consultationLocalDate;
        try {
            // Convert Date to LocalDate
            java.util.Date selectedDate = dateChooser.getDate();
            consultationLocalDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid! Gunakan format YYYY-MM-DD");
            return;
        }

        // Cek ketersediaan jadwal dokter
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ambil hari dalam seminggu dari tanggal konsultasi
            String dayOfWeek = consultationLocalDate.getDayOfWeek().name();

            // Konversi ke nama hari Indonesia
            String indoDay = ConsultationScheduleHelper.getIndonesianDay(dayOfWeek);

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

    private void addSchedule() {
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        String startTimeStr = formatTimeFromTimePicker(tpStartTime);
        String endTimeStr = formatTimeFromTimePicker(tpEndTime);

        if (cbPatient.getSelectedItem() == null || cbPatient.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pasien harus dipilih!");
            cbPatient.requestFocus();
            return;
        }

        if (cbDoctor.getSelectedItem() == null || cbDoctor.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dokter harus dipilih!");
            cbDoctor.requestFocus();
            return;
        }

        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Tanggal konsultasi harus dipilih!");
            dateChooser.requestFocus();
            return;
        }

        try {
            LocalDate consultationDate = dateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            if (consultationDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Tanggal konsultasi tidak valid (harus hari ini atau di masa depan)!");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid! Gunakan format YYYY-MM-DD");
            return;
        }

        try {
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);
            if (startTime.isAfter(endTime)) {
                JOptionPane.showMessageDialog(this, "Waktu mulai harus sebelum waktu selesai!");
                return;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format waktu tidak valid! Gunakan format HH:MM");
            return;
        }

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

            // Get date from JDateChooser
            LocalDate consultationDate = dateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            // Validasi ketersediaan dokter untuk waktu yang dipilih
            if (!controller.isDoctorAvailableForTime(doctorCode,
                                         consultationDate,
                                         LocalTime.parse(startTimeStr),
                                         LocalTime.parse(endTimeStr))) {
                JOptionPane.showMessageDialog(this, "Dokter tidak tersedia pada waktu yang dipilih!");
                return;
            }

            // Periksa apakah ada konflik jadwal untuk pasien
            if (controller.isPatientBusyAtTime(patientCode,
                                   consultationDate,
                                   LocalTime.parse(startTimeStr),
                                   LocalTime.parse(endTimeStr))) {
                JOptionPane.showMessageDialog(this, "Pasien sudah memiliki jadwal lain pada waktu yang dipilih!");
                return;
            }

            ConsultationSchedule schedule = new ConsultationSchedule();
            schedule.setPatientCode(patientCode);
            schedule.setPatientName(patientName);
            schedule.setDoctorCode(doctorCode);
            schedule.setDoctorName(doctorName);
            schedule.setConsultationDate(java.sql.Timestamp.valueOf(
                consultationDate.atTime(0, 0)));
            schedule.setStartTime(startTimeStr);
            schedule.setEndTime(endTimeStr);
            schedule.setStatus((String) cbStatus.getSelectedItem());
            // Extract room number from the selected room format "room_number (room_type)"
            String roomInfo = (String) cbRoom.getSelectedItem();
            String roomNumber = "";
            if (roomInfo != null && !roomInfo.isEmpty()) {
                int endIndex = roomInfo.indexOf(' ');
                if (endIndex > 0) {
                    roomNumber = roomInfo.substring(0, endIndex);
                } else {
                    roomNumber = roomInfo;
                }
            }
            // Tambahkan data kebutuhan rawat inap
            schedule.setInpatientRequired("Ya".equals(cbInpatientRequired.getSelectedItem()));

            if (dataAccess.insertSchedule(schedule)) {
                JOptionPane.showMessageDialog(this, "Jadwal konsultasi berhasil ditambahkan!");
                loadSchedules();
                clearForm();
                loadAvailableRoomsToComboBox(); // Refresh daftar kamar tersedia
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

        String startTimeStr = formatTimeFromTimePicker(tpStartTime);
        String endTimeStr = formatTimeFromTimePicker(tpEndTime);

        if (cbPatient.getSelectedItem() == null || cbPatient.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pasien harus dipilih!");
            cbPatient.requestFocus();
            return;
        }

        if (cbDoctor.getSelectedItem() == null || cbDoctor.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dokter harus dipilih!");
            cbDoctor.requestFocus();
            return;
        }

        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Tanggal konsultasi harus dipilih!");
            dateChooser.requestFocus();
            return;
        }

        try {
            LocalDate consultationDate = dateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            if (consultationDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Tanggal konsultasi tidak valid (harus hari ini atau di masa depan)!");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid! Gunakan format YYYY-MM-DD");
            return;
        }

        try {
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);
            if (startTime.isAfter(endTime)) {
                JOptionPane.showMessageDialog(this, "Waktu mulai harus sebelum waktu selesai!");
                return;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format waktu tidak valid! Gunakan format HH:MM");
            return;
        }

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

            // Get date from JDateChooser
            LocalDate consultationLocalDate = dateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            // Validasi ketersediaan dokter untuk waktu yang dipilih
            if (!controller.isDoctorAvailableForTime(doctorCode,
                                         consultationLocalDate,
                                         LocalTime.parse(startTimeStr),
                                         LocalTime.parse(endTimeStr))) {
                JOptionPane.showMessageDialog(this, "Dokter tidak tersedia pada waktu yang dipilih!");
                return;
            }

            // Periksa apakah ada konflik jadwal untuk pasien (kecuali jadwal yang sedang diupdate)
            if (controller.isPatientBusyAtTime(patientCode,
                                   consultationLocalDate,
                                   LocalTime.parse(startTimeStr),
                                   LocalTime.parse(endTimeStr),
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
                consultationLocalDate.atTime(0, 0)));
            schedule.setStartTime(startTimeStr);
            schedule.setEndTime(endTimeStr);
            schedule.setStatus((String) cbStatus.getSelectedItem());
            // Extract room number from the selected room format "room_number (room_type)"
            String roomInfo = (String) cbRoom.getSelectedItem();
            String roomNumber = "";
            if (roomInfo != null && !roomInfo.isEmpty()) {
                int endIndex = roomInfo.indexOf(' ');
                if (endIndex > 0) {
                    roomNumber = roomInfo.substring(0, endIndex);
                } else {
                    roomNumber = roomInfo;
                }
            }
            schedule.setRoom(roomNumber);

            // Tambahkan data kebutuhan rawat inap
            schedule.setInpatientRequired("Ya".equals(cbInpatientRequired.getSelectedItem()));

            if (dataAccess.updateScheduleInDB(schedule)) {
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
            if (dataAccess.deleteScheduleFromDB(selectedScheduleId)) {
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
        dateChooser.setDate(null);
        tpStartTime.setTime(java.time.LocalTime.now()); // Set to current time
        tpEndTime.setTime(java.time.LocalTime.now()); // Set to current time
        cbRoomType.setSelectedIndex(-1);
        cbRoom.setSelectedIndex(-1);
        cbStatus.setSelectedItem("Scheduled");
        cbInpatientRequired.setSelectedItem("Tidak");
        selectedScheduleId = -1;
        consultationTable.clearSelection();

        cbDoctor.removeAllItems(); // Keep doctor combobox empty until date is selected
        // Don't load all rooms - keep room combobox empty until room type is selected
        cbRoom.removeAllItems();
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
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            try {
                java.util.Date date = sdf.parse(dateStr.substring(0, 10));
                dateChooser.setDate(date);
                // After setting date, trigger the update to filter doctors for that date
                updateDoctorsForSelectedDate();
            } catch (Exception e) {
                dateChooser.setDate(null);
            }

            String timeStr = (String) tableModel.getValueAt(selectedRow, 6);
            String[] times = timeStr.split(" - ");
            if (times.length == 2) {
                try {
                    String startTimeStr = times[0];
                    String endTimeStr = times[1];
                    // Convert the HH:mm string to LocalTime objects
                    java.time.LocalTime startTime = java.time.LocalTime.parse(startTimeStr + ":00"); // Add seconds
                    java.time.LocalTime endTime = java.time.LocalTime.parse(endTimeStr + ":00"); // Add seconds

                    tpStartTime.setTime(startTime);
                    tpEndTime.setTime(endTime);
                } catch (Exception e) {
                    System.out.println("Error parsing time: " + e.getMessage());
                    tpStartTime.setTime(java.time.LocalTime.now());
                    tpEndTime.setTime(java.time.LocalTime.now());
                }
            }
            cbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 7));

            // Set the room based on the stored room number
            String roomNumber = (String) tableModel.getValueAt(selectedRow, 8);
            if (roomNumber != null && !roomNumber.isEmpty()) {
                // First, find the room type for this room number to set the room type combobox
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "SELECT room_type FROM rooms WHERE room_number = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, roomNumber);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        String roomType = rs.getString("room_type");
                        cbRoomType.setSelectedItem(roomType);
                        // Now update rooms for this type so the specific room will be available
                        updateRoomsForType();

                        // Find the room in the combobox that matches the stored room number
                        for (int i = 0; i < cbRoom.getItemCount(); i++) {
                            String roomInfo = cbRoom.getItemAt(i);
                            if (roomInfo.startsWith(roomNumber + " ")) {
                                cbRoom.setSelectedItem(roomInfo);
                                break;
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Load data tambahan untuk kebutuhan rawat inap
            Object inpatientRequiredObj = tableModel.getValueAt(selectedRow, 9); // kolom ke-9 adalah inpatient_required
            boolean inpatientRequired = inpatientRequiredObj != null && Boolean.TRUE.equals(inpatientRequiredObj);
            cbInpatientRequired.setSelectedItem(inpatientRequired ? "Ya" : "Tidak");
        }
    }

    private void loadSchedules() {
        dataAccess.loadSchedules(tableModel);
    }

    // Method untuk membuat booking kamar dari hasil konsultasi
    private void createBookingFromConsultation() {
        if (selectedScheduleId == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih konsultasi terlebih dahulu!");
            return;
        }

        // Cek apakah pasien perlu rawat inap
        String inpatientRequired = (String) cbInpatientRequired.getSelectedItem();
        if (!"Ya".equals(inpatientRequired)) {
            JOptionPane.showMessageDialog(this, "Pasien tidak membutuhkan rawat inap sesuai dengan hasil konsultasi ini!");
            return;
        }

        // Tampilkan dialog untuk membuat booking kamar
        // Di sini kita bisa membuka panel booking atau menampilkan form langsung
        int confirm = JOptionPane.showConfirmDialog(this,
                "Pasien membutuhkan rawat inap. Apakah Anda ingin membuat booking kamar sekarang?",
                "Konfirmasi Booking Kamar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Ambil informasi yang diperlukan
            String patientCode = "";
            String patientName = "";
            String selectedPatient = (String) cbPatient.getSelectedItem();
            if (selectedPatient != null && !selectedPatient.isEmpty()) {
                String[] parts = selectedPatient.split(" - ", 2);
                if (parts.length >= 2) {
                    patientCode = parts[0].trim();
                    patientName = parts[1].trim();
                }
            }

            String doctorCode = "";
            String doctorName = "";
            String selectedDoctor = (String) cbDoctor.getSelectedItem();
            if (selectedDoctor != null && !selectedDoctor.isEmpty()) {
                String[] parts = selectedDoctor.split(" - ", 2);
                if (parts.length >= 2) {
                    doctorCode = parts[0].trim();
                    doctorName = parts[1].trim();
                }
            }

            // For now, we'll pass empty values since the fields are removed
            String recommendedRoomType = "";
            String admissionNotes = "";

            // Get consultation date from date chooser
            String consultationDateStr = "";
            if (dateChooser.getDate() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                consultationDateStr = sdf.format(dateChooser.getDate());
            }

            // Buka form booking kamar dengan informasi dari konsultasi
            openBookingForm(patientCode, patientName, doctorCode, doctorName, recommendedRoomType, consultationDateStr, admissionNotes);
        }
    }

    private void loadRoomTypes() {
        cbRoomType.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT DISTINCT room_type FROM rooms ORDER BY room_type";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cbRoomType.addItem(rs.getString("room_type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAvailableRoomsToComboBox() {
        cbRoom.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT room_number, room_type FROM rooms WHERE status = 'Tersedia' ORDER BY room_number";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String roomInfo = rs.getString("room_number") + " (" + rs.getString("room_type") + ")";
                cbRoom.addItem(roomInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openBookingForm(String patientCode, String patientName, String doctorCode, String doctorName,
                                String recommendedRoomType, String consultationDate, String admissionNotes) {
        // Membuat dialog untuk form booking berdasarkan hasil konsultasi
        JDialog bookingDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Booking Kamar dari Konsultasi", true);
        bookingDialog.setLayout(new BorderLayout());

        // Membuat form booking kamar
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        formPanel.add(new JLabel("Kode Pasien:"), gbc);
        JTextField txtPatientCode = new JTextField(15);
        txtPatientCode.setText(patientCode);
        txtPatientCode.setEditable(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtPatientCode, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Nama Pasien:"), gbc);
        JTextField txtPatientName = new JTextField(15);
        txtPatientName.setText(patientName);
        txtPatientName.setEditable(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtPatientName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tipe Kamar Rekomendasi:"), gbc);
        JTextField txtRoomType = new JTextField(15);
        txtRoomType.setText(recommendedRoomType);
        txtRoomType.setEditable(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtRoomType, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tanggal Check-in:"), gbc);
        JTextField txtCheckInDate = new JTextField(15);
        txtCheckInDate.setText(consultationDate); // Gunakan tanggal konsultasi sebagai default
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtCheckInDate, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tanggal Check-out:"), gbc);
        JTextField txtCheckOutDate = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtCheckOutDate, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Catatan:"), gbc);
        JTextField txtNotes = new JTextField(15);
        txtNotes.setText(admissionNotes);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtNotes, gbc);

        // Tombol OK dan Batal
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Batal");

        btnOK.addActionListener(e -> {
            String checkInDate = txtCheckInDate.getText().trim();
            String checkOutDate = txtCheckOutDate.getText().trim();

            if (checkInDate.isEmpty()) {
                JOptionPane.showMessageDialog(bookingDialog, "Tanggal check-in harus diisi!");
                txtCheckInDate.requestFocus();
                return;
            }

            // Validasi format tanggal
            try {
                java.time.LocalDate.parse(checkInDate);
                if (!checkOutDate.isEmpty()) {
                    java.time.LocalDate.parse(checkOutDate);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(bookingDialog, "Format tanggal tidak valid! Gunakan format YYYY-MM-DD");
                return;
            }

            // Buka RoomBookingPanel dengan data yang sudah diisi
            SwingUtilities.invokeLater(() -> {
                // Di sini seharusnya kita buka RoomBookingPanel dan isi dengan data dari konsultasi
                // Kita bisa mengirim data ini lewat parameter atau method setter
                // Karena tidak bisa langsung mengakses RoomBookingPanel dari sini, kita tampilkan pesan untuk resepsionis
                JOptionPane.showMessageDialog(bookingDialog,
                    "Silakan buka panel Booking Kamar dan masukkan data pasien:\n" +
                    "- Kode Pasien: " + patientCode + "\n" +
                    "- Nama Pasien: " + patientName + "\n" +
                    "- Tipe Kamar Rekomendasi: " + recommendedRoomType + "\n" +
                    "- Tanggal Check-in: " + checkInDate + "\n" +
                    "- Catatan: " + admissionNotes);
            });

            bookingDialog.dispose();
        });

        btnCancel.addActionListener(e -> bookingDialog.dispose());

        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);

        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        bookingDialog.add(formPanel, BorderLayout.CENTER);
        bookingDialog.add(buttonPanel, BorderLayout.SOUTH);

        bookingDialog.setSize(400, 300);
        bookingDialog.setLocationRelativeTo(this);
        bookingDialog.setVisible(true);
    }
}