package rumahsakitjiwa.view;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.Pasien;

public class PasienCRUDPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable pasienTable;
    private JTextField txtPatientCode, txtFullName, txtAddress, txtPhone;
    private JTextField txtBirthDate, txtEmergencyContact, txtEmergencyPhone;
    private JComboBox<String> cbGender, cbActive;
    private JTextArea txtMedicalHistory;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedPasienId = -1;
    private Dashboard dashboard;

    public PasienCRUDPanel() {
        initComponents();
        setupTable();
        loadPasienData();
    }

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
    JLabel titleLabel = new JLabel("Form Data Pasien");
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    titleLabel.setForeground(new Color(0x6da395));
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
    formPanel.add(titleLabel, gbc);

    gbc.gridwidth = 1;
    
    // Label pada kolom 0 tetap default fill dan weightx
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;

    // Patient Code Label
    gbc.gridx = 0; gbc.gridy = 1;
    formPanel.add(new JLabel("Kode Pasien:"), gbc);
    // Patient Code Field dengan fill horizontal dan weightx 1.0
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtPatientCode = new JTextField(15);
    formPanel.add(txtPatientCode, gbc);

    // Nama Lengkap Label
    gbc.gridx = 0; gbc.gridy = 2;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("Nama Lengkap:"), gbc);
    // Nama Lengkap Field
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtFullName = new JTextField(15);
    formPanel.add(txtFullName, gbc);

    // Tanggal Lahir Label
    gbc.gridx = 0; gbc.gridy = 3;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("Tanggal Lahir (DD-MM-YYYY):"), gbc);
    // Tanggal Lahir Field
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtBirthDate = new JTextField(15);
    txtBirthDate.setText("dd-MM-yyyy");
    txtBirthDate.setForeground(Color.GRAY);

    txtBirthDate.addFocusListener(new java.awt.event.FocusListener() {
        @Override
        public void focusGained(java.awt.event.FocusEvent e) {
            if (txtBirthDate.getText().equals("dd-MM-yyyy")) {
                txtBirthDate.setText("");
                txtBirthDate.setForeground(Color.BLACK);
            }
        }
        @Override
        public void focusLost(java.awt.event.FocusEvent e) {
            if (txtBirthDate.getText().trim().isEmpty()) {
                txtBirthDate.setText("dd-MM-yyyy");
                txtBirthDate.setForeground(Color.GRAY);
            }
        }
    });

    formPanel.add(txtBirthDate, gbc);

    // Jenis Kelamin Label
    gbc.gridx = 0; gbc.gridy = 4;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("Jenis Kelamin:"), gbc);
    // ComboBox Jenis Kelamin
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    cbGender = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
    formPanel.add(cbGender, gbc);

    // Alamat Label
    gbc.gridx = 0; gbc.gridy = 5;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("Alamat:"), gbc);
    // Alamat Field
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtAddress = new JTextField(15);
    formPanel.add(txtAddress, gbc);

    // No. Telepon Label
    gbc.gridx = 0; gbc.gridy = 6;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("No. Telepon:"), gbc);
    // No. Telepon Field
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtPhone = new JTextField(15);
    formPanel.add(txtPhone, gbc);

    // Kontak Darurat Label
    gbc.gridx = 0; gbc.gridy = 7;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("Kontak Darurat:"), gbc);
    // Kontak Darurat Field
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtEmergencyContact = new JTextField(15);
    formPanel.add(txtEmergencyContact, gbc);

    // Telp. Darurat Label
    gbc.gridx = 0; gbc.gridy = 8;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("Telp. Darurat:"), gbc);
    // Telp. Darurat Field
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtEmergencyPhone = new JTextField(15);
    formPanel.add(txtEmergencyPhone, gbc);

    // Label Riwayat Medis di row 9, lebar 2 kolom
gbc.gridx = 0;
gbc.gridy = 9;
gbc.gridwidth = 2;
gbc.fill = GridBagConstraints.NONE;
gbc.weightx = 0;
gbc.weighty = 0;
gbc.anchor = GridBagConstraints.WEST;
formPanel.add(new JLabel("Riwayat Medis:"), gbc);

// TextArea Riwayat Medis di row 10, lebar 2 kolom, isi ruang vertikal maksimal
gbc.gridx = 0;
gbc.gridy = 10;
gbc.gridwidth = 2;
gbc.fill = GridBagConstraints.BOTH;
gbc.weightx = 1.0;
gbc.weighty = 1.0;
txtMedicalHistory = new JTextArea(6, 20);
txtMedicalHistory.setLineWrap(true);
txtMedicalHistory.setWrapStyleWord(true);
JScrollPane scrollHistory = new JScrollPane(txtMedicalHistory);
formPanel.add(scrollHistory, gbc);

// Button panel di row 11, lebar 2 kolom, tidak mendapat weighty sehingga tidak ter-expand
gbc.gridx = 0;
gbc.gridy = 11;
gbc.gridwidth = 2;
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.weightx = 1.0;
gbc.weighty = 0;
gbc.anchor = GridBagConstraints.CENTER;
gbc.insets = new Insets(10, 0, 0, 0); // beri jarak atas antara textarea dan tombol

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

formPanel.add(buttonPanel, gbc);
buttonPanel.add(btnAdd);
buttonPanel.add(btnUpdate);
buttonPanel.add(btnDelete);
buttonPanel.add(btnClear);

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

        // Setup table - Updated columns to match database
        String[] columns = {"ID", "Kode Pasien", "Nama Lengkap", "Tanggal Lahir", "Jenis Kelamin", 
                            "Alamat", "Telepon", "Kontak Darurat", "Telp. Darurat", 
                            "Riwayat Medis"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pasienTable = new JTable(tableModel);
        pasienTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pasienTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedPasien();
            }
        });

        // Style table
        JTableHeader header = pasienTable.getTableHeader();
        header.setBackground(new Color(0x96A78D));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        pasienTable.setRowHeight(25);
        pasienTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pasienTable.setGridColor(new Color(200, 200, 200));
        pasienTable.setSelectionBackground(new Color(0x6da395));

        // Set column widths
        pasienTable.getColumnModel().getColumn(0).setMaxWidth(0); // Hide ID
        pasienTable.getColumnModel().getColumn(0).setMinWidth(0);
        pasienTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(pasienTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private void setupTable() {
        // Hide ID column
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

    private void addPasien() {
        if (validateInput()) {
            try {
                Pasien pasien = new Pasien();
                pasien.setPatientCode(txtPatientCode.getText().trim());
                pasien.setFullName(txtFullName.getText().trim());
                
                String birthDateStr = txtBirthDate.getText().trim();
                if (!birthDateStr.equals("dd-MM-yyyy")) {
                    pasien.setBirthDateFromString(birthDateStr);
                }
                
                pasien.setGender((String) cbGender.getSelectedItem());
                pasien.setAddress(txtAddress.getText().trim());
                pasien.setPhone(txtPhone.getText().trim());
                pasien.setEmergencyContact(txtEmergencyContact.getText().trim());
                pasien.setEmergencyPhone(txtEmergencyPhone.getText().trim());
                pasien.setMedicalHistory(txtMedicalHistory.getText().trim());

                if (insertPasien(pasien)) {
                    JOptionPane.showMessageDialog(this, "Data pasien berhasil ditambahkan!");
                    loadPasienData();
                    clearForm();
                    // Refresh dashboard jika ada referensinya
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
        if (selectedPasienId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pasien yang akan diupdate!");
            return;
        }
        if (validateInput()) {
            try {
                Pasien pasien = new Pasien();
                pasien.setId(selectedPasienId);
                pasien.setPatientCode(txtPatientCode.getText().trim());
                pasien.setFullName(txtFullName.getText().trim());
                
                String birthDateStr = txtBirthDate.getText().trim();
                if (!birthDateStr.equals("dd-MM-yyyy")) {
                    pasien.setBirthDateFromString(birthDateStr);
                }
                
                pasien.setGender((String) cbGender.getSelectedItem());
                pasien.setAddress(txtAddress.getText().trim());
                pasien.setPhone(txtPhone.getText().trim());
                pasien.setEmergencyContact(txtEmergencyContact.getText().trim());
                pasien.setEmergencyPhone(txtEmergencyPhone.getText().trim());
                pasien.setMedicalHistory(txtMedicalHistory.getText().trim());

                if (updatePasienInDB(pasien)) {
                    JOptionPane.showMessageDialog(this, "Data pasien berhasil diupdate!");
                    loadPasienData();
                    clearForm();
                    // Refresh dashboard jika ada referensinya
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
        if (selectedPasienId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pasien yang akan dihapus!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus data pasien ini?", 
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (deletePasienFromDB(selectedPasienId)) {
                JOptionPane.showMessageDialog(this, "Data pasien berhasil dihapus!");
                loadPasienData();
                clearForm();
                // Refresh dashboard jika ada referensinya
                if (dashboard != null) {
                    dashboard.refreshDashboard();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data pasien!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtPatientCode.setText("");
        txtFullName.setText("");
        txtBirthDate.setText("dd-MM-yyyy");
        txtBirthDate.setForeground(Color.GRAY);
        cbGender.setSelectedIndex(0);
        txtAddress.setText("");
        txtPhone.setText("");
        txtEmergencyContact.setText("");
        txtEmergencyPhone.setText("");
        txtMedicalHistory.setText("");
        selectedPasienId = -1;
        pasienTable.clearSelection();
    }

    private boolean validateInput() {
        if (txtPatientCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode Pasien tidak boleh kosong!");
            txtPatientCode.requestFocus();
            return false;
        }
        if (txtFullName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Lengkap tidak boleh kosong!");
            txtFullName.requestFocus();
            return false;
        }
        
        // Validate birth date format
        String birthDateStr = txtBirthDate.getText().trim();
        if (!birthDateStr.equals("dd-MM-yyyy")) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate.parse(birthDateStr, formatter);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Format tanggal lahir tidak valid! Gunakan format DD-MM-YYYY");
                txtBirthDate.requestFocus();
                return false;
            }
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
        
        if (txtEmergencyContact.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kontak Darurat tidak boleh kosong!");
            txtEmergencyContact.requestFocus();
            return false;
        }
        if (txtEmergencyPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Telp. Darurat tidak boleh kosong!");
            txtEmergencyPhone.requestFocus();
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
            
            String birthDate = (String) tableModel.getValueAt(selectedRow, 3);
            if (birthDate == null || birthDate.isEmpty()) {
                txtBirthDate.setText("dd-MM-yyyy");
                txtBirthDate.setForeground(Color.GRAY);
            } else {
                txtBirthDate.setText(birthDate);
                txtBirthDate.setForeground(Color.BLACK);
            }
            
            String gender = (String) tableModel.getValueAt(selectedRow, 4);
            cbGender.setSelectedIndex(gender.equals("Laki-laki") ? 0 : 1);
            
            txtAddress.setText((String) tableModel.getValueAt(selectedRow, 5));
            txtPhone.setText((String) tableModel.getValueAt(selectedRow, 6));
            txtEmergencyContact.setText((String) tableModel.getValueAt(selectedRow, 7));
            txtEmergencyPhone.setText((String) tableModel.getValueAt(selectedRow, 8));
            
            String medicalHistory = (String) tableModel.getValueAt(selectedRow, 9);
            if (medicalHistory == null) medicalHistory = "";
            txtMedicalHistory.setText(medicalHistory);
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
                    rs.getDate("birth_date") != null ? 
                        rs.getDate("birth_date").toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "",
                    rs.getString("gender"),
                    rs.getString("address"),
                    rs.getString("phone"),
                    rs.getString("emergency_contact"),
                    rs.getString("emergency_phone"),
                    rs.getString("medical_history")
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
            String sql = "INSERT INTO patients (patient_code, full_name, birth_date, gender, address, phone, " +
                         "emergency_contact, emergency_phone, medical_history) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pasien.getPatientCode());
            pstmt.setString(2, pasien.getFullName());
            
            if (pasien.getBirthDate() != null) {
                pstmt.setDate(3, Date.valueOf(pasien.getBirthDate()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            
            pstmt.setString(4, pasien.getGender());
            pstmt.setString(5, pasien.getAddress());
            pstmt.setString(6, pasien.getPhone());
            pstmt.setString(7, pasien.getEmergencyContact());
            pstmt.setString(8, pasien.getEmergencyPhone());
            pstmt.setString(9, pasien.getMedicalHistory());

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
            String sql = "UPDATE patients SET patient_code=?, full_name=?, birth_date=?, gender=?, address=?, phone=?, " +
                         "emergency_contact=?, emergency_phone=?, medical_history=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pasien.getPatientCode());
            pstmt.setString(2, pasien.getFullName());
            
            if (pasien.getBirthDate() != null) {
                pstmt.setDate(3, Date.valueOf(pasien.getBirthDate()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            
            pstmt.setString(4, pasien.getGender());
            pstmt.setString(5, pasien.getAddress());
            pstmt.setString(6, pasien.getPhone());
            pstmt.setString(7, pasien.getEmergencyContact());
            pstmt.setString(8, pasien.getEmergencyPhone());
            pstmt.setString(9, pasien.getMedicalHistory());
            pstmt.setInt(10, pasien.getId());

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
}