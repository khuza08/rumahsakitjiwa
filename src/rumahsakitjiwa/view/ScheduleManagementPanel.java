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
    private JComboBox<Integer> cbDoctors;
    private JTextField txtDoctorCode; // <-- Field baru
    private JCheckBox[] dayBoxes;
    private JComboBox<String> cbShift;
    private JButton btnAddSchedule, btnUpdateSchedule, btnDeleteSchedule, btnClearForm;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private int selectedScheduleId = -1;
    private Dashboard dashboard;
    private boolean isFirstLoad = true;

    private static final String[] SHIFTS = {
        "Pagi (06:00 - 14:00)",
        "Siang (14:00 - 22:00)",
        "Malam (22:00 - 06:00)"
    };

    // Simpan info dokter: ID -> {nama, kode}
    private static class DoctorInfo {
        String name;
        String code;
        DoctorInfo(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }
    private Map<Integer, DoctorInfo> doctorInfoMap = new HashMap<>();

    public ScheduleManagementPanel() {
        initComponents();
        loadDoctors();
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
        cbDoctors.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Pilih Dokter");
                    setForeground(Color.GRAY); // Opsional: warna abu-abu untuk placeholder
                } else if (value instanceof Integer) {
                    Integer id = (Integer) value;
                    DoctorInfo info = doctorInfoMap.get(id);
                    setText(info != null ? info.name : "Dokter Tidak Diketahui");
                    setForeground(Color.BLACK); // Reset warna
                }
                return this;
            }
        });
        cbDoctors.addActionListener(e -> updateDoctorCode());
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(cbDoctors, gbc);

        // Kode Dokter (read-only)
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Kode Dokter:"), gbc);
        txtDoctorCode = new JTextField(15);
        txtDoctorCode.setEditable(false);          // Tidak bisa diedit
        txtDoctorCode.setFocusable(false);         // Tidak bisa dapat fokus (tidak bisa diklik)
        txtDoctorCode.setBackground(UIManager.getColor("Label.background")); // Tampilan seperti label
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtDoctorCode, gbc);

        // Hari jaga
        gbc.gridx = 0; gbc.gridy = 2;
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
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Shift:"), gbc);
        cbShift = new JComboBox<>(SHIFTS);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(cbShift, gbc);

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

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Table
        String[] columns = {"ID", "Dokter", "Hari", "Shift"};
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

    private void updateDoctorCode() {
        Object selected = cbDoctors.getSelectedItem();
        if (selected instanceof Integer) {
            DoctorInfo info = doctorInfoMap.get((Integer) selected);
            txtDoctorCode.setText(info != null ? info.code : "");
        } else {
            txtDoctorCode.setText(""); // Jika null atau tidak valid
        }
    }

    private void loadDoctors() {
        doctorInfoMap.clear();
        cbDoctors.removeAllItems(); // Hapus semua item

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, full_name, doctor_code FROM doctors ORDER BY full_name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("full_name");
                String code = rs.getString("doctor_code");
                doctorInfoMap.put(id, new DoctorInfo(name, code));
                cbDoctors.addItem(id); // Tambahkan ID dokter saja
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar dokter: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Set default: null (untuk trigger placeholder)
        cbDoctors.setSelectedItem(null);
    }

    private void resetFormToDefault() {
        cbDoctors.setSelectedItem(null); // ← Ini akan trigger placeholder
        txtDoctorCode.setText("");
        for (JCheckBox box : dayBoxes) box.setSelected(false);
        cbShift.setSelectedIndex(0);
    }

    private void loadSchedules() {
        isFirstLoad = true;
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.id, d.full_name, s.days, s.shift " +
                         "FROM schedules s JOIN doctors d ON s.doctor_id = d.id ORDER BY d.full_name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("days"),
                    rs.getString("shift")
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
        String doctorName = (String) tableModel.getValueAt(selectedRow, 1);

        // Cari ID berdasarkan nama
        int doctorId = -1;
        for (Map.Entry<Integer, DoctorInfo> entry : doctorInfoMap.entrySet()) {
            if (entry.getValue().name.equals(doctorName)) {
                doctorId = entry.getKey();
                break;
            }
        }
        if (doctorId != -1) {
            cbDoctors.setSelectedItem(doctorId);
        } else {
            cbDoctors.setSelectedItem(null); // Jika tidak ditemukan, kosongkan
        }
        // Kode dokter akan otomatis update via listener

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
        Object selected = cbDoctors.getSelectedItem();
        if (selected == null) { // ← Bukan -1, tapi null
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
            int doctorId = (Integer) cbDoctors.getSelectedItem();
            String days = getSelectedDays();
            String shift = (String) cbShift.getSelectedItem();

            Schedule schedule = new Schedule();
            schedule.setDoctorId(doctorId);
            schedule.setDays(days);
            schedule.setShift(shift);

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
            int doctorId = (Integer) cbDoctors.getSelectedItem();
            String days = getSelectedDays();
            String shift = (String) cbShift.getSelectedItem();

            Schedule schedule = new Schedule();
            schedule.setId(selectedScheduleId);
            schedule.setDoctorId(doctorId);
            schedule.setDays(days);
            schedule.setShift(shift);

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
    // (Tidak berubah, tetap sama seperti sebelumnya)
    private boolean insertSchedule(Schedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO schedules (doctor_id, days, shift) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, schedule.getDoctorId());
            pstmt.setString(2, schedule.getDays());
            pstmt.setString(3, schedule.getShift());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updateScheduleInDB(Schedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE schedules SET doctor_id=?, days=?, shift=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, schedule.getDoctorId());
            pstmt.setString(2, schedule.getDays());
            pstmt.setString(3, schedule.getShift());
            pstmt.setInt(4, schedule.getId());
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