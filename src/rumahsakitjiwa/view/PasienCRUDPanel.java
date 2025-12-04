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
import rumahsakitjiwa.model.Pasien;

public class PasienCRUDPanel extends JPanel {
    // Kelas untuk membatasi input hanya angka pada NIK
    private static class NumericDocument extends javax.swing.text.PlainDocument {
        @Override
        public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
            if (str == null) return;

            // Hanya izinkan karakter angka
            for (char c : str.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return; // Jangan masukkan karakter non-digit
                }
            }

            super.insertString(offs, str, a);
        }
    }

    // Kelas untuk membatasi input hanya angka minimal 10 digit pada No. Telepon
    private static class PhoneDocument extends javax.swing.text.PlainDocument {
        private final int minLength;

        public PhoneDocument(int minLength) {
            this.minLength = minLength;
        }

        @Override
        public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
            if (str == null) return;

            // Ambil teks saat ini
            String currentText = getText(0, getLength());
            String newText = currentText.substring(0, offs) + str + currentText.substring(offs);

            // Pastikan semua karakter adalah angka
            for (char c : newText.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return; // Jangan masukkan karakter non-digit
                }
            }

            // Periksa panjang teks tidak melebihi maksimum yang wajar (misalnya 15 digit)
            if (newText.length() > 15) {
                return; // Jangan masukkan karakter jika sudah melebihi 15 digit
            }

            super.insertString(offs, str, a);
        }
    }
    private JTabbedPane tabbedPane;
    private DefaultTableModel tableModel;
    private JTable pasienTable;
    private JTextField txtPatientCode, txtFullName, txtAddress, txtPhone;
    private JTextField txtFamilyName, txtFamilyAddress, txtNIK;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedPasienId = -1;
    private Dashboard dashboard;
    private String userRole;

    public PasienCRUDPanel() {
        initComponents();
        setupTable();
        loadPasienData();
        applyRolePermissions(); // terapkan izin default (view-only)
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
        txtFullName.setEditable(isResepsionis);
        txtAddress.setEditable(isResepsionis);
        txtPhone.setEditable(isResepsionis);
        txtFamilyName.setEditable(isResepsionis);
        txtFamilyAddress.setEditable(isResepsionis);
        txtNIK.setEditable(isResepsionis);

        // Sembunyikan tombol CRUD untuk admin
        if (btnAdd != null) btnAdd.setVisible(isResepsionis);
        if (btnUpdate != null) btnUpdate.setVisible(isResepsionis);
        if (btnDelete != null) btnDelete.setVisible(isResepsionis);
        if (btnClear != null) btnClear.setVisible(isResepsionis);
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Buat tabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(Color.WHITE);
        
        // Tab untuk data pasien
        JPanel pasienPanel = createPasienPanel();
        tabbedPane.addTab("Data Pasien", pasienPanel);
        
        // Tab untuk pasien inap
        PasienInapPanel pasienInapPanel = new PasienInapPanel();
        pasienInapPanel.setUserRole(userRole);
        tabbedPane.addTab("Pasien Inap", pasienInapPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createPasienPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);
        
        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        return mainPanel;
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

        JLabel titleLabel = new JLabel("Form Data Pasien");
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

        // Nama Lengkap
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtFullName = new JTextField(15);
        formPanel.add(txtFullName, gbc);

        // NIK
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("NIK:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtNIK = new JTextField(15);
        txtNIK.setDocument(new NumericDocument()); // Terapkan custom document untuk hanya angka
        formPanel.add(txtNIK, gbc);

        // Alamat
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Alamat:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtAddress = new JTextField(15);
        formPanel.add(txtAddress, gbc);

        // No. Telepon
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("No. Telepon:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtPhone = new JTextField(15);
        txtPhone.setDocument(new PhoneDocument(10)); // Terapkan custom document untuk hanya angka minimal 10 digit
        formPanel.add(txtPhone, gbc);

        // Nama Keluarga
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Nama Keluarga:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtFamilyName = new JTextField(15);
        formPanel.add(txtFamilyName, gbc);

        // Alamat Keluarga
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Alamat Keluarga:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtFamilyAddress = new JTextField(15);
        formPanel.add(txtFamilyAddress, gbc);

        // Button Panel
        gbc.gridx = 0;
        gbc.gridy = 8;
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

        btnAdd.addActionListener(e -> addPasien());
        btnUpdate.addActionListener(e -> updatePasien());
        btnDelete.addActionListener(e -> deletePasien());
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

        JLabel tableTitle = new JLabel("Daftar Pasien");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(0x6da395));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "Kode Pasien", "Nama Lengkap", "NIK", "Alamat", "Telepon", "Nama Keluarga", "Alamat Keluarga"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pasienTable = new JTable(tableModel);
        pasienTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pasienTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedPasien();
            }
        });

        JTableHeader header = pasienTable.getTableHeader();
        header.setBackground(new Color(0x96A78D));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        pasienTable.setRowHeight(25);
        pasienTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pasienTable.setGridColor(new Color(200, 200, 200));
        pasienTable.setSelectionBackground(new Color(0x6da395));

        pasienTable.getColumnModel().getColumn(0).setMinWidth(0);
        pasienTable.getColumnModel().getColumn(0).setMaxWidth(0);
        pasienTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(pasienTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private void setupTable() {
        pasienTable.getColumnModel().getColumn(0).setMinWidth(0);
        pasienTable.getColumnModel().getColumn(0).setMaxWidth(0);
        pasienTable.getColumnModel().getColumn(0).setPreferredWidth(0);
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

    private String generatePatientCode() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT MAX(CAST(SUBSTRING(patient_code, 4) AS SIGNED)) AS max_num FROM patients WHERE patient_code LIKE 'PSN%'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            int nextNumber = 1;
            if (rs.next()) {
                Integer maxNum = rs.getObject("max_num", Integer.class);
                if (maxNum != null) {
                    nextNumber = maxNum + 1;
                }
            }
            return String.format("PSN%03d", nextNumber);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal generate kode pasien: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return "PSN001";
        }
    }

    private void addPasien() {
        // <<< TAMBAHKAN GUARD INI
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        String fullName = txtFullName.getText().trim();
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Lengkap tidak boleh kosong!");
            txtFullName.requestFocus();
            return;
        }

        if (isFullNameExists(fullName, -1)) {
            JOptionPane.showMessageDialog(this, "Nama pasien sudah terdaftar! Silakan gunakan nama yang berbeda.");
            txtFullName.requestFocus();
            return;
        }

        if (validateInput()) {
            try {
                Pasien pasien = new Pasien();
                String autoCode = generatePatientCode();
                pasien.setPatientCode(autoCode);
                pasien.setFullName(txtFullName.getText().trim());
                pasien.setNIK(txtNIK.getText().trim());
                pasien.setAddress(txtAddress.getText().trim());
                pasien.setPhone(txtPhone.getText().trim());
                pasien.setFamilyName(txtFamilyName.getText().trim());
                pasien.setFamilyAddress(txtFamilyAddress.getText().trim());

                if (insertPasien(pasien)) {
                    JOptionPane.showMessageDialog(this, "Data pasien berhasil ditambahkan!");
                    loadPasienData();
                    clearForm();
                    if (dashboard != null) {
                        dashboard.refreshDashboard();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menambah data pasien!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updatePasien() {
        // <<< TAMBAHKAN GUARD INI
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        if (selectedPasienId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pasien yang akan diupdate!");
            return;
        }

        String fullName = txtFullName.getText().trim();
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Lengkap tidak boleh kosong!");
            txtFullName.requestFocus();
            return;
        }

        if (isFullNameExists(fullName, selectedPasienId)) {
            JOptionPane.showMessageDialog(this, "Nama pasien sudah terdaftar! Silakan gunakan nama yang berbeda.");
            txtFullName.requestFocus();
            return;
        }

        if (validateInput()) {
            try {
                Pasien pasien = new Pasien();
                pasien.setId(selectedPasienId);
                pasien.setPatientCode(txtPatientCode.getText().trim());
                pasien.setFullName(txtFullName.getText().trim());
                pasien.setNIK(txtNIK.getText().trim());
                pasien.setAddress(txtAddress.getText().trim());
                pasien.setPhone(txtPhone.getText().trim());
                pasien.setFamilyName(txtFamilyName.getText().trim());
                pasien.setFamilyAddress(txtFamilyAddress.getText().trim());

                if (updatePasienInDB(pasien)) {
                    JOptionPane.showMessageDialog(this, "Data pasien berhasil diupdate!");
                    loadPasienData();
                    clearForm();
                    if (dashboard != null) {
                        dashboard.refreshDashboard();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengupdate data pasien!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deletePasien() {
        // <<< TAMBAHKAN GUARD INI
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        int[] selectedRows = pasienTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Pilih pasien yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus " + selectedRows.length + " data pasien?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = true;
            for (int row : selectedRows) {
                int id = (Integer) tableModel.getValueAt(row, 0);
                if (!deletePasienFromDB(id)) {
                    success = false;
                }
            }
            if (success) {
                JOptionPane.showMessageDialog(this, "Data pasien berhasil dihapus!");
                loadPasienData();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Sebagian data gagal dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtPatientCode.setText("");
        txtFullName.setText("");
        txtNIK.setText("");
        txtAddress.setText("");
        txtPhone.setText("");
        txtFamilyName.setText("");
        txtFamilyAddress.setText("");
        selectedPasienId = -1;
        pasienTable.clearSelection();
    }

    private boolean validateInput() {
        if (txtNIK.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIK tidak boleh kosong!");
            txtNIK.requestFocus();
            return false;
        }

        // Validasi NIK hanya angka (jika sudah dimasukkan)
        String nik = txtNIK.getText().trim();
        if (!nik.isEmpty() && !nik.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "NIK harus berisi angka saja!");
            txtNIK.requestFocus();
            return false;
        }

        if (txtAddress.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alamat tidak boleh kosong!");
            txtAddress.requestFocus();
            return false;
        }

        if (txtPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No. Telepon tidak boleh kosong!");
            txtPhone.requestFocus();
            return false;
        }

        // Validasi panjang nomor telepon minimal 10 digit
        String phone = txtPhone.getText().trim();
        if (phone.length() < 10) {
            JOptionPane.showMessageDialog(this, "No. Telepon harus minimal 10 digit angka!");
            txtPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void loadSelectedPasien() {
        int selectedRow = pasienTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedPasienId = (Integer) tableModel.getValueAt(selectedRow, 0);
            txtPatientCode.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtFullName.setText((String) tableModel.getValueAt(selectedRow, 2));
            txtNIK.setText((String) tableModel.getValueAt(selectedRow, 3));
            txtAddress.setText((String) tableModel.getValueAt(selectedRow, 4));
            txtPhone.setText((String) tableModel.getValueAt(selectedRow, 5));
            txtFamilyName.setText((String) tableModel.getValueAt(selectedRow, 6));
            txtFamilyAddress.setText((String) tableModel.getValueAt(selectedRow, 7));
        }
    }

    private void loadPasienData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM patients ORDER BY patient_code";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("patient_code"),
                        rs.getString("full_name"),
                        rs.getString("nik"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("family_name"),
                        rs.getString("family_address")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data pasien: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean insertPasien(Pasien pasien) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO patients (patient_code, full_name, nik, address, phone, " +
                    "family_name, family_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pasien.getPatientCode());
            pstmt.setString(2, pasien.getFullName());
            pstmt.setString(3, pasien.getNIK());
            pstmt.setString(4, pasien.getAddress());
            pstmt.setString(5, pasien.getPhone());
            pstmt.setString(6, pasien.getFamilyName());
            pstmt.setString(7, pasien.getFamilyAddress());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updatePasienInDB(Pasien pasien) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE patients SET patient_code=?, full_name=?, nik=?, address=?, phone=?, " +
                    "family_name=?, family_address=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pasien.getPatientCode());
            pstmt.setString(2, pasien.getFullName());
            pstmt.setString(3, pasien.getNIK());
            pstmt.setString(4, pasien.getAddress());
            pstmt.setString(5, pasien.getPhone());
            pstmt.setString(6, pasien.getFamilyName());
            pstmt.setString(7, pasien.getFamilyAddress());
            pstmt.setInt(8, pasien.getId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean deletePasienFromDB(int pasienId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM patients WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pasienId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean isFullNameExists(String fullName, int excludeId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM patients WHERE full_name = ? AND id != ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullName.trim());
            pstmt.setInt(2, excludeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}