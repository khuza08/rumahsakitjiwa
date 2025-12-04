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
    private JTextField txtDoctorCode;
    private JCheckBox[] dayBoxes;
    private JComboBox<String> cbShift;
    private JButton btnAddSchedule, btnUpdateSchedule, btnDeleteSchedule, btnClearForm;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private int selectedScheduleId = -1; // Tetap simpan ID internal untuk CRUD
    private Dashboard dashboard;
    private boolean isFirstLoad = true;
    private JTextField searchField;

    private static final Map<String, String[]> SHIFT_TIMES = new HashMap<>();
    static {
        SHIFT_TIMES.put("Pagi (06:00 - 14:00)", new String[]{"06:00:00", "14:00:00"});
        SHIFT_TIMES.put("Siang (14:00 - 22:00)", new String[]{"14:00:00", "22:00:00"});
        SHIFT_TIMES.put("Malam (22:00 - 06:00)", new String[]{"22:00:00", "06:00:00"});
    }

    private static class DoctorInfo {
        String name;
        String code;
        DoctorInfo(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }
    private Map<String, DoctorInfo> doctorInfoMap = new HashMap<>();

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
                    setForeground(Color.GRAY);
                } else if (value instanceof String) {
                    String code = (String) value;
                    DoctorInfo info = doctorInfoMap.get(code);
                    setText(info != null ? info.name : "Dokter Tidak Diketahui");
                    setForeground(Color.BLACK);
                }
                return this;
            }
        });
        cbDoctors.addActionListener(e -> updateDoctorCode());
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(cbDoctors, gbc);

        // Kode Dokter
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Kode Dokter:"), gbc);
        txtDoctorCode = new JTextField(15);
        txtDoctorCode.setEditable(false);
        txtDoctorCode.setFocusable(false);
        txtDoctorCode.setBackground(UIManager.getColor("Label.background"));
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
        cbShift = new JComboBox<>(SHIFT_TIMES.keySet().toArray(new String[0]));
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

        // Tambahkan panel pencarian di sebelah kanan tombol
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        searchPanel.add(new JLabel("Cari Jadwal:"));
        searchField = new JTextField(20);
        searchField.setToolTipText("Cari berdasarkan kode dokter, nama dokter, hari, atau shift");
        searchPanel.add(searchField);

        // Gabungkan panel tombol dan pencarian dalam satu panel horizontal
        JPanel buttonAndSearchPanel = new JPanel(new BorderLayout());
        buttonAndSearchPanel.add(buttonPanel, BorderLayout.CENTER);
        buttonAndSearchPanel.add(searchPanel, BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(buttonAndSearchPanel, gbc);

        // ✅ GANTI KOLOM JADI "Kode Dokter"
        String[] columns = {"Kode Dokter", "Dokter", "Hari", "Shift"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        scheduleTable = new JTable(tableModel);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Buat TableRowSorter dan tetapkan ke tabel
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> rowSorter =
            new javax.swing.table.TableRowSorter<>(tableModel);
        scheduleTable.setRowSorter(rowSorter);

        scheduleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && scheduleTable.getSelectedRow() != -1 && !isFirstLoad) {
                loadSelectedSchedule();
            }
        });

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Tambahkan listener untuk live search
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTable(searchField.getText());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTable(searchField.getText());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTable(searchField.getText());
            }
        });

        resetFormToDefault();
    }

    private void updateDoctorCode() {
        Object selected = cbDoctors.getSelectedItem();
        if (selected instanceof String) {
            DoctorInfo info = doctorInfoMap.get((String) selected);
            txtDoctorCode.setText(info != null ? info.code : "");
        } else {
            txtDoctorCode.setText("");
        }
    }

    private void loadDoctors() {
        doctorInfoMap.clear();
        cbDoctors.removeAllItems();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT doctor_code, full_name FROM doctors ORDER BY full_name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String code = rs.getString("doctor_code");
                String name = rs.getString("full_name");
                doctorInfoMap.put(code, new DoctorInfo(name, code));
                cbDoctors.addItem(code);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar dokter: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        cbDoctors.setSelectedItem(null);
    }

    private void resetFormToDefault() {
        cbDoctors.setSelectedItem(null);
        txtDoctorCode.setText("");
        for (JCheckBox box : dayBoxes) box.setSelected(false);
        cbShift.setSelectedIndex(0);
    }

    private void loadSchedules() {
        isFirstLoad = true;

        // Simpan status filter saat ini
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter =
            (javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel>) scheduleTable.getRowSorter();
        javax.swing.RowFilter rowFilter = null;
        if (sorter != null) {
            rowFilter = sorter.getRowFilter();
        }

        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            // ✅ Ambil doctor_code dari tabel doctors
            String sql = "SELECT s.id, d.doctor_code, d.full_name, s.days, s.shift " +
                         "FROM schedules s JOIN doctors d ON s.doctor_code = d.doctor_code ORDER BY d.full_name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // ✅ Tampilkan doctor_code di kolom pertama
                Object[] row = {
                    rs.getString("doctor_code"), // ← Kolom "Kode Dokter"
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

        // Setelah memuat data baru, kembalikan filter jika ada
        if (sorter != null) {
            sorter.setRowFilter(rowFilter);
        }

        resetFormToDefault();
        selectedScheduleId = -1;
        scheduleTable.clearSelection();
        isFirstLoad = false;
    }

    private void loadSelectedSchedule() {
        int selectedViewRow = scheduleTable.getSelectedRow();
        if (selectedViewRow < 0) return;

        // Konversi dari indeks tampilan ke indeks model sebenarnya
        int selectedModelRow = scheduleTable.convertRowIndexToModel(selectedViewRow);

        // ✅ Ambil schedule.id berdasarkan doctor_code + hari + shift
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.id FROM schedules s " +
                         "WHERE s.doctor_code = ? AND s.days = ? AND s.shift = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, (String) tableModel.getValueAt(selectedModelRow, 0)); // Kode Dokter
            pstmt.setString(2, (String) tableModel.getValueAt(selectedModelRow, 2)); // Hari
            pstmt.setString(3, (String) tableModel.getValueAt(selectedModelRow, 3)); // Shift
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                selectedScheduleId = rs.getInt("id");
            } else {
                selectedScheduleId = -1;
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            selectedScheduleId = -1;
            return;
        }

        // Load dokter
        String doctorCode = (String) tableModel.getValueAt(selectedModelRow, 0);
        cbDoctors.setSelectedItem(doctorCode);

        // Load days
        String daysStr = (String) tableModel.getValueAt(selectedModelRow, 2);
        Set<String> selectedDays = new HashSet<>();
        if (daysStr != null && !daysStr.trim().isEmpty()) {
            selectedDays.addAll(Arrays.asList(daysStr.split(",")));
        }
        for (JCheckBox box : dayBoxes) {
            box.setSelected(selectedDays.contains(box.getText()));
        }

        // Load shift
        String shift = (String) tableModel.getValueAt(selectedModelRow, 3);
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
        if (selected == null) {
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
            String doctorCode = (String) cbDoctors.getSelectedItem();
            String days = getSelectedDays();
            String shiftText = (String) cbShift.getSelectedItem();

            String[] times = SHIFT_TIMES.get(shiftText);
            if (times == null) {
                JOptionPane.showMessageDialog(this, "Shift tidak valid!");
                return;
            }

            java.sql.Time startTime = java.sql.Time.valueOf(times[0]);
            java.sql.Time endTime = java.sql.Time.valueOf(times[1]);

            Schedule schedule = new Schedule();
            schedule.setDoctorCode(doctorCode);
            schedule.setDays(days);
            schedule.setShift(shiftText);
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);

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
            String doctorCode = (String) cbDoctors.getSelectedItem();
            String days = getSelectedDays();
            String shiftText = (String) cbShift.getSelectedItem();

            String[] times = SHIFT_TIMES.get(shiftText);
            if (times == null) {
                JOptionPane.showMessageDialog(this, "Shift tidak valid!");
                return;
            }

            java.sql.Time startTime = java.sql.Time.valueOf(times[0]);
            java.sql.Time endTime = java.sql.Time.valueOf(times[1]);

            Schedule schedule = new Schedule();
            schedule.setId(selectedScheduleId);
            schedule.setDoctorCode(doctorCode);
            schedule.setDays(days);
            schedule.setShift(shiftText);
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);

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
            String sql = "INSERT INTO schedules (doctor_code, days, shift, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getDoctorCode());
            pstmt.setString(2, schedule.getDays());
            pstmt.setString(3, schedule.getShift());
            pstmt.setTime(4, schedule.getStartTime());
            pstmt.setTime(5, schedule.getEndTime());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updateScheduleInDB(Schedule schedule) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE schedules SET doctor_code=?, days=?, shift=?, start_time=?, end_time=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getDoctorCode());
            pstmt.setString(2, schedule.getDays());
            pstmt.setString(3, schedule.getShift());
            pstmt.setTime(4, schedule.getStartTime());
            pstmt.setTime(5, schedule.getEndTime());
            pstmt.setInt(6, schedule.getId());
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

    private void filterTable(String searchText) {
        // Konversi teks pencarian ke huruf kecil untuk pencarian case-insensitive
        String lowerSearchText = searchText.toLowerCase().trim();

        // Pastikan sorter sudah diinisialisasi
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> rowSorter
            = (javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel>) scheduleTable.getRowSorter();

        if (rowSorter == null) {
            // Jika sorter belum diinisialisasi, buat baru dan tetapkan ke tabel
            rowSorter = new javax.swing.table.TableRowSorter<>(tableModel);
            scheduleTable.setRowSorter(rowSorter);
        }

        if (lowerSearchText.isEmpty()) {
            // Jika tidak ada teks pencarian, tampilkan semua data
            rowSorter.setRowFilter(null);
        } else {
            // Buat filter untuk mencocokkan teks di semua kolom
            rowSorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(lowerSearchText)));
        }
    }
}