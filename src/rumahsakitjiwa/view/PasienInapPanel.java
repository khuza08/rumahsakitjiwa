package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import rumahsakitjiwa.database.DatabaseConnection;
import com.toedter.calendar.JDateChooser;

public class PasienInapPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable pasienInapTable;
    private JTextField txtPatientCode, txtFullName, txtRoomNumber;
    private JDateChooser txtAdmissionDate, txtDischargeDate;
    private JTextArea txtDiagnosis, txtTreatment;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedPasienInapId = -1;
    private String userRole;

    public PasienInapPanel() {
        initComponents();
        setupTable();
        loadPasienInapData();
        applyRolePermissions(); 
    }

    // <<< TAMBAHKAN METHOD INI
    public void setUserRole(String role) {
        this.userRole = role;
        applyRolePermissions();
    }

    // <<< TAMBAHKAN METHOD INI
    private void applyRolePermissions() {
        boolean isResepsionis = "resepsionis".equalsIgnoreCase(userRole);

        // Nonaktifkan input untuk admin
        txtRoomNumber.setEditable(isResepsionis);
        txtAdmissionDate.setEnabled(isResepsionis);
        txtDischargeDate.setEnabled(isResepsionis);
        txtDiagnosis.setEditable(isResepsionis);
        txtTreatment.setEditable(isResepsionis);

        // Sembunyikan tombol CRUD untuk admin
        if (btnAdd != null) btnAdd.setVisible(isResepsionis);
        if (btnUpdate != null) btnUpdate.setVisible(isResepsionis);
        if (btnDelete != null) btnDelete.setVisible(isResepsionis);
        if (btnClear != null) btnClear.setVisible(isResepsionis);
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
        formPanel.setPreferredSize(new Dimension(350, 0));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Form Data Pasien Inap");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0x6da395));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Patient Code (disabled)
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Kode Pasien:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtPatientCode = new JTextField(15);
        txtPatientCode.setEditable(false);
        txtPatientCode.setBackground(Color.LIGHT_GRAY);
        formPanel.add(txtPatientCode, gbc);

        // Nama Lengkap (disabled)
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtFullName = new JTextField(15);
        txtFullName.setEditable(false);
        txtFullName.setBackground(Color.LIGHT_GRAY);
        formPanel.add(txtFullName, gbc);

        // No. Kamar
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("No. Kamar:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtRoomNumber = new JTextField(15);
        formPanel.add(txtRoomNumber, gbc);

        // Tanggal Masuk
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Tanggal Masuk:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtAdmissionDate = new JDateChooser();
        txtAdmissionDate.setDateFormatString("dd-MM-yyyy");
        formPanel.add(txtAdmissionDate, gbc);

        // Tanggal Keluar
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Tanggal Keluar:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtDischargeDate = new JDateChooser();
        txtDischargeDate.setDateFormatString("dd-MM-yyyy");
        formPanel.add(txtDischargeDate, gbc);

        // Diagnosis Label
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Diagnosis:"), gbc);

        // Diagnosis TextArea
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        txtDiagnosis = new JTextArea(4, 20);
        txtDiagnosis.setLineWrap(true);
        txtDiagnosis.setWrapStyleWord(true);
        JScrollPane scrollDiagnosis = new JScrollPane(txtDiagnosis);
        formPanel.add(scrollDiagnosis, gbc);

        // Treatment Label
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Treatment:"), gbc);

        // Treatment TextArea
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        txtTreatment = new JTextArea(4, 20);
        txtTreatment.setLineWrap(true);
        txtTreatment.setWrapStyleWord(true);
        JScrollPane scrollTreatment = new JScrollPane(txtTreatment);
        formPanel.add(scrollTreatment, gbc);

        // Button Panel
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setOpaque(false);

        btnAdd = createStyledButton("Tambah", new Color(0x4CAF50));
        btnUpdate = createStyledButton("Update", new Color(0x2196F3));
        btnDelete = createStyledButton("Hapus", new Color(0xF44336));
        btnClear = createStyledButton("Clear", new Color(0x9E9E9E));

        btnAdd.addActionListener(e -> addPasienInap());
        btnUpdate.addActionListener(e -> updatePasienInap());
        btnDelete.addActionListener(e -> deletePasienInap());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
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

        JLabel tableTitle = new JLabel("Daftar Pasien Inap");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(0x6da395));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "Kode Pasien", "Nama Lengkap", "No. Kamar", "Tanggal Masuk", "Tanggal Keluar", "Diagnosis"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pasienInapTable = new JTable(tableModel);
        pasienInapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pasienInapTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedPasienInap();
            }
        });

        JTableHeader header = pasienInapTable.getTableHeader();
        header.setBackground(new Color(0x96A78D));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        pasienInapTable.setRowHeight(25);
        pasienInapTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pasienInapTable.setGridColor(new Color(200, 200, 200));
        pasienInapTable.setSelectionBackground(new Color(0x6da395));

        pasienInapTable.getColumnModel().getColumn(0).setMinWidth(0);
        pasienInapTable.getColumnModel().getColumn(0).setMaxWidth(0);
        pasienInapTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(pasienInapTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private void setupTable() {
        pasienInapTable.getColumnModel().getColumn(0).setMinWidth(0);
        pasienInapTable.getColumnModel().getColumn(0).setMaxWidth(0);
        pasienInapTable.getColumnModel().getColumn(0).setPreferredWidth(0);
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

    private void addPasienInap() {
        // <<< TAMBAHKAN GUARD INI
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        String patientCode = txtPatientCode.getText().trim();
        if (patientCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu!");
            return;
        }

        if (validateInput()) {
            try {
                if (insertPasienInap()) {
                    JOptionPane.showMessageDialog(this, "Data pasien inap berhasil ditambahkan!");
                    loadPasienInapData();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menambah data pasien inap!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updatePasienInap() {
        // <<< TAMBAHKAN GUARD INI
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        if (selectedPasienInapId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pasien inap yang akan diupdate!");
            return;
        }

        if (validateInput()) {
            try {
                if (updatePasienInapInDB()) {
                    JOptionPane.showMessageDialog(this, "Data pasien inap berhasil diupdate!");
                    loadPasienInapData();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengupdate data pasien inap!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deletePasienInap() {
        // <<< TAMBAHKAN GUARD INI
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        int selectedRow = pasienInapTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pasien inap yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus data pasien inap ini?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (deletePasienInapFromDB()) {
                JOptionPane.showMessageDialog(this, "Data pasien inap berhasil dihapus!");
                loadPasienInapData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data pasien inap!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtPatientCode.setText("");
        txtFullName.setText("");
        txtRoomNumber.setText("");
        txtAdmissionDate.setDate(null);
        txtDischargeDate.setDate(null);
        txtDiagnosis.setText("");
        txtTreatment.setText("");
        selectedPasienInapId = -1;
        pasienInapTable.clearSelection();
    }

    private boolean validateInput() {
        if (txtRoomNumber.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No. Kamar tidak boleh kosong!");
            txtRoomNumber.requestFocus();
            return false;
        }
        if (txtAdmissionDate.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Tanggal Masuk harus dipilih!");
            txtAdmissionDate.requestFocus();
            return false;
        }
        if (txtDiagnosis.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Diagnosis tidak boleh kosong!");
            txtDiagnosis.requestFocus();
            return false;
        }
        if (txtTreatment.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Treatment tidak boleh kosong!");
            txtTreatment.requestFocus();
            return false;
        }

        return true;
    }

    private void loadSelectedPasienInap() {
        int selectedRow = pasienInapTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedPasienInapId = (Integer) tableModel.getValueAt(selectedRow, 0);
            txtPatientCode.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtFullName.setText((String) tableModel.getValueAt(selectedRow, 2));
            txtRoomNumber.setText((String) tableModel.getValueAt(selectedRow, 3));

            String admissionDateStr = (String) tableModel.getValueAt(selectedRow, 4);
            if (admissionDateStr == null || admissionDateStr.isEmpty()) {
                txtAdmissionDate.setDate(null);
            } else {
                try {
                    LocalDate date = LocalDate.parse(admissionDateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    txtAdmissionDate.setDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                } catch (Exception e) {
                    txtAdmissionDate.setDate(null);
                }
            }

            String dischargeDateStr = (String) tableModel.getValueAt(selectedRow, 5);
            if (dischargeDateStr == null || dischargeDateStr.isEmpty()) {
                txtDischargeDate.setDate(null);
            } else {
                try {
                    LocalDate date = LocalDate.parse(dischargeDateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    txtDischargeDate.setDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                } catch (Exception e) {
                    txtDischargeDate.setDate(null);
                }
            }

            txtDiagnosis.setText((String) tableModel.getValueAt(selectedRow, 6));
            // Treatment tidak ditampilkan di tabel, jadi perlu diambil dari database
            loadTreatmentFromDB(selectedPasienInapId);
        }
    }

    private void loadTreatmentFromDB(int pasienInapId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT treatment FROM inpatient_records WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pasienInapId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                txtTreatment.setText(rs.getString("treatment"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPasienInapData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT ir.id, p.patient_code, p.full_name, ir.room_number, " +
                    "ir.admission_date, ir.discharge_date, ir.diagnosis " +
                    "FROM inpatient_records ir " +
                    "JOIN patients p ON ir.patient_id = p.id " +
                    "ORDER BY ir.admission_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("patient_code"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getDate("admission_date") != null ?
                                rs.getDate("admission_date").toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "",
                        rs.getDate("discharge_date") != null ?
                                rs.getDate("discharge_date").toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "",
                        rs.getString("diagnosis")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data pasien inap: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean insertPasienInap() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get patient ID from patient code
            String sqlGetPatientId = "SELECT id FROM patients WHERE patient_code = ?";
            PreparedStatement pstmtGetPatientId = conn.prepareStatement(sqlGetPatientId);
            pstmtGetPatientId.setString(1, txtPatientCode.getText().trim());
            ResultSet rsPatient = pstmtGetPatientId.executeQuery();
            
            if (!rsPatient.next()) {
                JOptionPane.showMessageDialog(this, "Kode pasien tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            int patientId = rsPatient.getInt("id");
            
            // Insert inpatient record
            String sql = "INSERT INTO inpatient_records (patient_id, room_number, admission_date, discharge_date, diagnosis, treatment) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, patientId);
            pstmt.setString(2, txtRoomNumber.getText().trim());
            
            Date selectedAdmissionDate = txtAdmissionDate.getDate();
            if (selectedAdmissionDate != null) {
                LocalDate admissionDate = selectedAdmissionDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                pstmt.setDate(3, java.sql.Date.valueOf(admissionDate));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            
            Date selectedDischargeDate = txtDischargeDate.getDate();
            if (selectedDischargeDate != null) {
                LocalDate dischargeDate = selectedDischargeDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                pstmt.setDate(4, java.sql.Date.valueOf(dischargeDate));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            
            pstmt.setString(5, txtDiagnosis.getText().trim());
            pstmt.setString(6, txtTreatment.getText().trim());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updatePasienInapInDB() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE inpatient_records SET room_number=?, admission_date=?, discharge_date=?, diagnosis=?, treatment=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtRoomNumber.getText().trim());
            
            Date selectedAdmissionDate = txtAdmissionDate.getDate();
            if (selectedAdmissionDate != null) {
                LocalDate admissionDate = selectedAdmissionDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                pstmt.setDate(2, java.sql.Date.valueOf(admissionDate));
            } else {
                pstmt.setNull(2, Types.DATE);
            }
            
            Date selectedDischargeDate = txtDischargeDate.getDate();
            if (selectedDischargeDate != null) {
                LocalDate dischargeDate = selectedDischargeDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                pstmt.setDate(3, java.sql.Date.valueOf(dischargeDate));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            
            pstmt.setString(4, txtDiagnosis.getText().trim());
            pstmt.setString(5, txtTreatment.getText().trim());
            pstmt.setInt(6, selectedPasienInapId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean deletePasienInapFromDB() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM inpatient_records WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedPasienInapId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // Method untuk memilih pasien dari tabel pasien
    public void selectPatient(String patientCode, String fullName) {
        txtPatientCode.setText(patientCode);
        txtFullName.setText(fullName);
    }
}