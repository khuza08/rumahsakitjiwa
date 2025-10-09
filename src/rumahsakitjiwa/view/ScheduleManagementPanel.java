package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import java.util.*;
import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.Schedule;

public class ScheduleManagementPanel extends JPanel {
    private JComboBox<String> cbDoctors;
    private JCheckBox[] dayBoxes; // Senin - Sabtu
    private JComboBox<String> cbShift;
    private JSpinner spMaxPatients;
    private JButton btnAddSchedule, btnUpdateSchedule, btnDeleteSchedule, btnClearForm;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private int selectedScheduleId = -1;
    private Dashboard dashboard;
    private boolean isFirstLoad = true;

    // Mapping shift
    private static final String[] SHIFTS = {
        "Pagi (06:00 - 14:00)",
        "Siang (14:00 - 22:00)",
        "Malam (22:00 - 06:00)"
    };

    public ScheduleManagementPanel() {
        initComponents();
        loadSchedules();
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Jadwal"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Doctor
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Dokter:"), gbc);
        cbDoctors = new JComboBox<>();
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(cbDoctors, gbc);

        // Hari jaga (checkbox)
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Hari Jaga:"), gbc);
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] days = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
        dayBoxes = new JCheckBox[6];
        for (int i = 0; i < 6; i++) {
            dayBoxes[i] = new JCheckBox(days[i]);
            dayPanel.add(dayBoxes[i]);
        }
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dayPanel, gbc);

        // Shift
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Shift:"), gbc);
        cbShift = new JComboBox<>(SHIFTS);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(cbShift, gbc);

        // Max patients → sekarang di gridy = 3
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Maks. Pasien:"), gbc);
        spMaxPatients = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(spMaxPatients, gbc);

        JComponent editor = spMaxPatients.getEditor();
        JFormattedTextField tf = ((JSpinner.NumberEditor) editor).getTextField();
        tf.setHorizontalAlignment(JTextField.LEFT);

        // Buttons → sekarang di gridy = 4
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

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Table
        String[] columns = {"ID", "Dokter", "Hari", "Shift", "Maks. Pasien"};
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
        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        resetFormToDefault();
    }

    private void resetFormToDefault() {
        cbDoctors.setSelectedIndex(-1);
        for (JCheckBox box : dayBoxes) box.setSelected(false);
        cbShift.setSelectedIndex(0);
        spMaxPatients.setValue(10);
    }

    private void loadSchedules() {
        isFirstLoad = true;
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.id, d.full_name, s.days, s.shift, s.max_patients " +
                         "FROM schedules s JOIN doctors d ON s.doctor_id = d.id ORDER BY d.full_name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("days"),
                    rs.getString("shift"),
                    rs.getInt("max_patients")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data jadwal: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        resetFormToDefault();
        selectedScheduleId = -1;
        scheduleTable.clearSelection();
        isFirstLoad = false;
    }

    private void loadSelectedSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow < 0) return;

        selectedScheduleId = (Integer) tableModel.getValueAt(selectedRow, 0);

        // Load doctor
        String doctorName = (String) tableModel.getValueAt(selectedRow, 1);
        for (int i = 0; i < cbDoctors.getItemCount(); i++) {
            String item = (String) cbDoctors.getItemAt(i);
            if (item != null && item.contains(doctorName)) {
                cbDoctors.setSelectedIndex(i);
                break;
            }
        }

        // Load days
        String daysStr = (String) tableModel.getValueAt(selectedRow, 2);
        Set<String> selectedDays = new HashSet<>();
        if (daysStr != null && !daysStr.trim().isEmpty()) {
            selectedDays.addAll(Arrays.asList(daysStr.split(",")));
        }
        for (JCheckBox box : dayBoxes) {
            box.setSelected(selectedDays.contains(box.getText()));
        }

        // Load shift
        String shift = (String) tableModel.getValueAt(selectedRow, 3);
        cbShift.setSelectedItem(shift);

        // Load max patients → indeks 4 (karena lokasi dihapus)
        spMaxPatients.setValue((Integer) tableModel.getValueAt(selectedRow, 4));
    }

    private String getSelectedDays() {
        List<String> selected = new ArrayList<>();
        for (JCheckBox box : dayBoxes) {
            if (box.isSelected()) {
                selected.add(box.getText());
            }
        }
        return String.join(",", selected);
    }

    private boolean validateInput() {
        if (cbDoctors.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dokter!");
            return false;
        }
        if (getSelectedDays().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih minimal satu hari jaga!");
            return false;
        }
        return true;
    }

    private void addSchedule() {
        if (!validateInput()) return;

        try {
            String doctorInfo = (String) cbDoctors.getSelectedItem();
            int doctorId = Integer.parseInt(doctorInfo.split(" - ")[0]);
            String days = getSelectedDays();
            String shift = (String) cbShift.getSelectedItem();
            int maxPatients = (Integer) spMaxPatients.getValue();

            Schedule schedule = new Schedule();
            schedule.setDoctorId(doctorId);
            schedule.setDays(days);
            schedule.setShift(shift);
            schedule.setMaxPatients(maxPatients);

            if (insertSchedule(schedule)) {
                JOptionPane.showMessageDialog(this, "Jadwal berhasil ditambahkan!");
                loadSchedules();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambah jadwal!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSchedule() {
        if (selectedScheduleId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang akan diupdate!");
            return;
        }

        if (!validateInput()) return;

        try {
            String doctorInfo = (String) cbDoctors.getSelectedItem();
            int doctorId = Integer.parseInt(doctorInfo.split(" - ")[0]);
            String days = getSelectedDays();
            String shift = (String) cbShift.getSelectedItem();
            int maxPatients = (Integer) spMaxPatients.getValue();

            Schedule schedule = new Schedule();
            schedule.setId(selectedScheduleId);
            schedule.setDoctorId(doctorId);
            schedule.setDays(days);
            schedule.setShift(shift);
            schedule.setMaxPatients(maxPatients);

            if (updateScheduleInDB(schedule)) {
                JOptionPane.showMessageDialog(this, "Jadwal berhasil diupdate!");
                loadSchedules();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengupdate jadwal!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                if (dashboard != null) dashboard.refreshDashboard();
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

    // --- DATABASE METHODS ---

    private boolean insertSchedule(Schedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // HANYA 4 kolom: doctor_id, days, shift, max_patients
            String sql = "INSERT INTO schedules (doctor_id, days, shift, max_patients) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, schedule.getDoctorId());
            pstmt.setString(2, schedule.getDays());
            pstmt.setString(3, schedule.getShift());
            pstmt.setInt(4, schedule.getMaxPatients()); // <-- indeks benar: 4
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updateScheduleInDB(Schedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // HANYA 4 kolom di SET, + WHERE id
            String sql = "UPDATE schedules SET doctor_id=?, days=?, shift=?, max_patients=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, schedule.getDoctorId());
            pstmt.setString(2, schedule.getDays());
            pstmt.setString(3, schedule.getShift());
            pstmt.setInt(4, schedule.getMaxPatients()); // <-- tidak ada setLocation!
            pstmt.setInt(5, schedule.getId());
            return pstmt.executeUpdate() > 0;
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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}