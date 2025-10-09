    package rumahsakitjiwa.view;

    import javax.swing.*;
    import javax.swing.border.TitledBorder;
    import javax.swing.table.DefaultTableModel;
    import java.awt.*;
    import java.sql.*;
    import java.time.DayOfWeek;
    import java.time.format.TextStyle;
    import java.util.Locale;
    import java.util.Date;
    import java.text.SimpleDateFormat;
    import javax.swing.SpinnerDateModel;
    import javax.swing.SpinnerNumberModel;
    import rumahsakitjiwa.database.DatabaseConnection;
    import rumahsakitjiwa.model.Schedule;

    public class ScheduleManagementPanel extends JPanel {
        private JComboBox<String> cbDoctors;
        private JComboBox<String> cbDaysOfWeek;
        private JSpinner spStartTime, spEndTime;
        private JTextField txtLocation;
        private JSpinner spMaxPatients;
        private JCheckBox chkActive;
        private JButton btnAddSchedule, btnUpdateSchedule, btnDeleteSchedule, btnClearForm;
        private JTable scheduleTable;
        private DefaultTableModel tableModel;
        private int selectedScheduleId = -1;
        private Dashboard dashboard; // Referensi ke Dashboard
        private boolean isFirstLoad = true; // Flag untuk mencegah pemilihan otomatis saat pertama kali
        private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        public ScheduleManagementPanel() {
            initComponents();
            loadSchedules();
        }

        // Metode setter untuk Dashboard
        public void setDashboard(Dashboard dashboard) {
            this.dashboard = dashboard;
        }

        private void initComponents() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Form Panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("Form Jadwal"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Doctor selection
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Dokter:"), gbc);
            cbDoctors = new JComboBox<>();
            gbc.gridx = 1; gbc.weightx = 1.0;
            formPanel.add(cbDoctors, gbc);

            // Day of week
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            formPanel.add(new JLabel("Hari:"), gbc);
            cbDaysOfWeek = new JComboBox<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                cbDaysOfWeek.addItem(day.getDisplayName(TextStyle.FULL, new Locale("id", "ID")));
            }
            gbc.gridx = 1; gbc.weightx = 1.0;
            formPanel.add(cbDaysOfWeek, gbc);

            // Time selection
            gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
            formPanel.add(new JLabel("Jam Mulai:"), gbc);
            spStartTime = new JSpinner(new SpinnerDateModel());
            spStartTime.setEditor(new JSpinner.DateEditor(spStartTime, "hh:mm a"));
            gbc.gridx = 1; gbc.weightx = 1.0;
            formPanel.add(spStartTime, gbc);

            gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
            formPanel.add(new JLabel("Jam Selesai:"), gbc);
            spEndTime = new JSpinner(new SpinnerDateModel());
            spEndTime.setEditor(new JSpinner.DateEditor(spEndTime, "hh:mm a"));
            gbc.gridx = 1; gbc.weightx = 1.0;
            formPanel.add(spEndTime, gbc);

            // Location
            gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
            formPanel.add(new JLabel("Lokasi:"), gbc);
            txtLocation = new JTextField(15);
            gbc.gridx = 1; gbc.weightx = 1.0;
            formPanel.add(txtLocation, gbc);

            // Max patients
            gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
            formPanel.add(new JLabel("Maks. Pasien:"), gbc);
            spMaxPatients = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
            gbc.gridx = 1; gbc.weightx = 1.0;
            formPanel.add(spMaxPatients, gbc);
            
            // Set max patients spinner input alignment to left
            JComponent editor = spMaxPatients.getEditor();
            JFormattedTextField tf = ((JSpinner.NumberEditor) editor).getTextField();
            tf.setHorizontalAlignment(JTextField.LEFT);

            // Active status
            gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
            formPanel.add(new JLabel("Aktif:"), gbc);
            chkActive = new JCheckBox();
            chkActive.setSelected(true);
            gbc.gridx = 1; gbc.weightx = 1.0;
            formPanel.add(chkActive, gbc);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            btnAddSchedule = new JButton("Tambah");
            btnUpdateSchedule = new JButton("Update");
            btnDeleteSchedule = new JButton("Hapus");
            btnClearForm = new JButton("Clear");

            btnAddSchedule.addActionListener(e -> addSchedule());
            btnUpdateSchedule.addActionListener(e -> updateSchedule());
            btnDeleteSchedule.addActionListener(e -> deleteSchedule());
            btnClearForm.addActionListener(e -> clearForm());

            buttonPanel.add(btnAddSchedule);
            buttonPanel.add(btnUpdateSchedule);
            buttonPanel.add(btnDeleteSchedule);
            buttonPanel.add(btnClearForm);

            gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.weightx = 1.0;
            formPanel.add(buttonPanel, gbc);

            // Table
            String[] columns = {"ID", "Dokter", "Hari", "Jam Mulai", "Jam Selesai", "Lokasi", "Maks. Pasien", "Status"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            scheduleTable = new JTable(tableModel);
            scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            scheduleTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && scheduleTable.getSelectedRow() != -1 && !isFirstLoad) {
                    loadSelectedSchedule();
                }
            });

            JScrollPane scrollPane = new JScrollPane(scheduleTable);

            // Add components to main panel
            add(formPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);

            // Set default values for form
            resetFormToDefault();
        }

        private void resetFormToDefault() {
            cbDoctors.setSelectedIndex(-1);
            cbDaysOfWeek.setSelectedIndex(-1);

            // Set default times - 9:00 AM and 10:00 PM
            Date now = new Date();

            // Set start time to 9:00 AM
            Date startTime = new Date();
            startTime.setHours(9);
            startTime.setMinutes(0);
            spStartTime.setValue(startTime);

            // Set end time to 10:00 PM
            Date endTime = new Date();
            endTime.setHours(22); // 22:00 is 10:00 PM in 24-hour format
            endTime.setMinutes(0);
            spEndTime.setValue(endTime);

            txtLocation.setText("");
            spMaxPatients.setValue(10);
            chkActive.setSelected(true);
        }


        private void loadSchedules() {
            isFirstLoad = true; // Set flag untuk mencegah pemilihan otomatis
            tableModel.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT s.id, d.full_name, s.day_of_week, s.start_time, s.end_time, s.location, s.max_patients " +
                             "FROM schedules s JOIN doctors d ON s.doctor_id = d.id ORDER BY d.full_name, s.day_of_week";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    // Format times to display in AM/PM format
                    Time startTime = rs.getTime("start_time");
                    Time endTime = rs.getTime("end_time");

                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("day_of_week"),
                        timeFormat.format(startTime),
                        timeFormat.format(endTime),
                        rs.getString("location"),
                        rs.getInt("max_patients"),
                    };
                    tableModel.addRow(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal memuat data jadwal: " + e.getMessage(), 
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }

            // Reset form ke nilai default setelah tabel dimuat
            resetFormToDefault();
            selectedScheduleId = -1;

            // Hapus selection pada tabel
            scheduleTable.clearSelection();

            // Reset flag setelah tabel selesai dimuat
            isFirstLoad = false;
        }

        private void loadSelectedSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedScheduleId = (Integer) tableModel.getValueAt(selectedRow, 0);

            // Set doctor - PERBAIKAN: Ambil doctor_id dari database berdasarkan schedule yang dipilih
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT doctor_id FROM schedules WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, selectedScheduleId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    int doctorId = rs.getInt("doctor_id");
                    // Cari item di combo box yang mengandung doctor_id ini
                    for (int i = 0; i < cbDoctors.getItemCount(); i++) {
                        String item = cbDoctors.getItemAt(i);
                        if (item.startsWith(doctorId + " - ")) {
                            cbDoctors.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Fallback: cari berdasarkan nama dokter
                String doctorName = (String) tableModel.getValueAt(selectedRow, 1);
                for (int i = 0; i < cbDoctors.getItemCount(); i++) {
                    String item = cbDoctors.getItemAt(i);
                    if (item.contains(doctorName)) {
                        cbDoctors.setSelectedIndex(i);
                        break;
                    }
                }
            }

            // Set day of week
            String day = (String) tableModel.getValueAt(selectedRow, 2);
            cbDaysOfWeek.setSelectedItem(day);

            // Parse times from AM/PM format to 24-hour format for spinner
            String startTimeStr = (String) tableModel.getValueAt(selectedRow, 3);
            String endTimeStr = (String) tableModel.getValueAt(selectedRow, 4);

            try {
                // Parse AM/PM format
                Date startTime = timeFormat.parse(startTimeStr);
                Date endTime = timeFormat.parse(endTimeStr);

                spStartTime.setValue(startTime);
                spEndTime.setValue(endTime);
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to current time if parsing fails
                spStartTime.setValue(new Date());
                spEndTime.setValue(new Date());
            }

            // Set location
            txtLocation.setText((String) tableModel.getValueAt(selectedRow, 5));

            // Set max patients - PERBAIKAN: Convert ke Integer dengan aman
            Object maxPatientsObj = tableModel.getValueAt(selectedRow, 6);
            if (maxPatientsObj instanceof Integer) {
                spMaxPatients.setValue((Integer) maxPatientsObj);
            } else if (maxPatientsObj instanceof String) {
                try {
                    spMaxPatients.setValue(Integer.parseInt((String) maxPatientsObj));
                } catch (NumberFormatException e) {
                    spMaxPatients.setValue(10); // default value
                }
            } else {
                spMaxPatients.setValue(10); // default value
            }

            // Set active status
            String status = (String) tableModel.getValueAt(selectedRow, 7);
            chkActive.setSelected(status.equals("Aktif"));
        }
    }

        private void addSchedule() {
            if (validateInput()) {
                try {
                    // Get doctor ID from combo box
                    String doctorInfo = (String) cbDoctors.getSelectedItem();
                    int doctorId = Integer.parseInt(doctorInfo.split(" - ")[0]);

                    // Get day of week
                    String dayOfWeek = (String) cbDaysOfWeek.getSelectedItem();

                    // Get times
                    Date startTime = (Date) spStartTime.getValue();
                    Date endTime = (Date) spEndTime.getValue();

                    // Format times to HH:MM:SS for database storage
                    java.sql.Time sqlStartTime = new java.sql.Time(startTime.getHours(), startTime.getMinutes(), 0);
                    java.sql.Time sqlEndTime = new java.sql.Time(endTime.getHours(), endTime.getMinutes(), 0);

                    // Get other values
                    String location = txtLocation.getText().trim();
                    int maxPatients = (Integer) spMaxPatients.getValue();
                    boolean isActive = chkActive.isSelected();

                    // Create schedule object
                    Schedule schedule = new Schedule();
                    schedule.setDoctorId(doctorId);
                    schedule.setDayOfWeek(dayOfWeek);
                    schedule.setStartTime(sqlStartTime);
                    schedule.setEndTime(sqlEndTime);
                    schedule.setLocation(location);
                    schedule.setMaxPatients(maxPatients);
                    schedule.setActive(isActive);

                    // Insert to database
                    if (insertSchedule(schedule)) {
                        JOptionPane.showMessageDialog(this, "Jadwal berhasil ditambahkan!");
                        loadSchedules();
                        // Refresh dashboard jika ada referensinya
                        if (dashboard != null) {
                            dashboard.refreshDashboard();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal menambah jadwal!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void updateSchedule() {
            if (selectedScheduleId == -1) {
                JOptionPane.showMessageDialog(this, "Pilih jadwal yang akan diupdate!");
                return;
            }

            if (validateInput()) {
                try {
                    // Get doctor ID from combo box
                    String doctorInfo = (String) cbDoctors.getSelectedItem();
                    int doctorId = Integer.parseInt(doctorInfo.split(" - ")[0]);

                    // Get day of week
                    String dayOfWeek = (String) cbDaysOfWeek.getSelectedItem();

                    // Get times
                    Date startTime = (Date) spStartTime.getValue();
                    Date endTime = (Date) spEndTime.getValue();

                    // Format times to HH:MM:SS for database storage
                    java.sql.Time sqlStartTime = new java.sql.Time(startTime.getHours(), startTime.getMinutes(), 0);
                    java.sql.Time sqlEndTime = new java.sql.Time(endTime.getHours(), endTime.getMinutes(), 0);

                    // Get other values
                    String location = txtLocation.getText().trim();
                    int maxPatients = (Integer) spMaxPatients.getValue();
                    boolean isActive = chkActive.isSelected();

                    // Create schedule object
                    Schedule schedule = new Schedule();
                    schedule.setId(selectedScheduleId);
                    schedule.setDoctorId(doctorId);
                    schedule.setDayOfWeek(dayOfWeek);
                    schedule.setStartTime(sqlStartTime);
                    schedule.setEndTime(sqlEndTime);
                    schedule.setLocation(location);
                    schedule.setMaxPatients(maxPatients);
                    schedule.setActive(isActive);

                    // Update in database
                    if (updateScheduleInDB(schedule)) {
                        JOptionPane.showMessageDialog(this, "Jadwal berhasil diupdate!");
                        loadSchedules();
                        // Refresh dashboard jika ada referensinya
                        if (dashboard != null) {
                            dashboard.refreshDashboard();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal mengupdate jadwal!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void deleteSchedule() {
            if (selectedScheduleId == -1) {
                JOptionPane.showMessageDialog(this, "Pilih jadwal yang akan dihapus!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin menghapus jadwal ini?", 
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (deleteScheduleFromDB(selectedScheduleId)) {
                    JOptionPane.showMessageDialog(this, "Jadwal berhasil dihapus!");
                    loadSchedules();
                    // Refresh dashboard jika ada referensinya
                    if (dashboard != null) {
                        dashboard.refreshDashboard();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus jadwal!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void clearForm() {
            resetFormToDefault();
            selectedScheduleId = -1;
            scheduleTable.clearSelection();
        }

        private boolean validateInput() {
            if (cbDoctors.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Pilih dokter!");
                cbDoctors.requestFocus();
                return false;
            }

            if (txtLocation.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lokasi tidak boleh kosong!");
                txtLocation.requestFocus();
                return false;
            }

            // Validate time range
            Date startTime = (Date) spStartTime.getValue();
            Date endTime = (Date) spEndTime.getValue();

            if (startTime.after(endTime) || startTime.equals(endTime)) {
                JOptionPane.showMessageDialog(this, "Jam mulai harus sebelum jam selesai!");
                spStartTime.requestFocus();
                return false;
            }

            return true;
        }

        private boolean insertSchedule(Schedule schedule) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO schedules (doctor_id, day_of_week, start_time, end_time, location, max_patients) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, schedule.getDoctorId());
                pstmt.setString(2, schedule.getDayOfWeek());
                pstmt.setTime(3, schedule.getStartTime());
                pstmt.setTime(4, schedule.getEndTime());
                pstmt.setString(5, schedule.getLocation());
                pstmt.setInt(6, schedule.getMaxPatients());
                pstmt.setBoolean(7, schedule.isActive());

                int result = pstmt.executeUpdate();
                return result > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        private boolean updateScheduleInDB(Schedule schedule) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE schedules SET doctor_id=?, day_of_week=?, start_time=?, end_time=?, location=?, max_patients=? WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, schedule.getDoctorId());
                pstmt.setString(2, schedule.getDayOfWeek());
                pstmt.setTime(3, schedule.getStartTime());
                pstmt.setTime(4, schedule.getEndTime());
                pstmt.setString(5, schedule.getLocation());
                pstmt.setInt(6, schedule.getMaxPatients());
                pstmt.setBoolean(7, schedule.isActive());
                pstmt.setInt(8, schedule.getId());

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
                String sql = "DELETE FROM schedules WHERE id=?";
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