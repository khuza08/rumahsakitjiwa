package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.Doctor;

public class DoctorCRUDPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable doctorTable;
    private JTextField txtDoctorCode, txtFullName, txtPhone, txtEmail, txtAddress; // ✅ txtDoctorCode ditambahkan, txtSpecialization dihapus
    // private JComboBox<String> cbActive; // ❌ dihapus
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private JLabel totalDoctorLabel; // ✅ untuk update total dokter
    private int selectedDoctorId = -1;
    private Dashboard dashboard;
    private JTextField searchField;
    
    // Filter dihapus karena tidak ada spesialisasi/status
    // private JComboBox<String> cbFilterSpecialization;
    // private JComboBox<String> cbFilterStatus;

    private static class NumericDocument extends PlainDocument {
        private final int maxLength;

        public NumericDocument(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null) return;
            String currentText = getText(0, getLength());
            String newText = currentText.substring(0, offs) + str + currentText.substring(offs);
            if (newText.matches("\\d+") && newText.length() <= maxLength) {
                super.insertString(offs, str, a);
            }
        }
    }

    // Kelas untuk membatasi input hanya karakter yang valid untuk email
    private static class EmailDocument extends PlainDocument {
        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null) return;

            // Ambil teks saat ini
            String currentText = getText(0, getLength());
            String newText = currentText.substring(0, offs) + str + currentText.substring(offs);

            // Periksa apakah karakter yang dimasukkan valid untuk email (hanya karakter alfanumerik, ., _, +, -, @)
            for (char c : str.toCharArray()) {
                if (!isEmailCharacter(c)) {
                    return; // Jangan masukkan karakter yang tidak valid untuk email
                }
            }

            super.insertString(offs, str, a);
        }

        private boolean isEmailCharacter(char c) {
            // Email hanya boleh berisi huruf, angka, dan karakter khusus: ., _, +, -, @
            return Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '+' || c == '-' || c == '@';
        }
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public DoctorCRUDPanel() {
        initComponents();
        setupTable();
        loadDoctorData();
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
        formPanel.setPreferredSize(new Dimension(350, 0));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Form Data Dokter");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0x6da395));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // ✅ Kode Dokter (ID Dokter) - disabled
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("ID Dokter:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtDoctorCode = new JTextField(15);
        txtDoctorCode.setEditable(false);
        txtDoctorCode.setBackground(Color.LIGHT_GRAY);
        formPanel.add(txtDoctorCode, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtFullName = new JTextField(15);
        formPanel.add(txtFullName, gbc);

        // ❌ Spesialisasi dihapus

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Alamat:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtAddress = new JTextField(15);
        formPanel.add(txtAddress, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("No. Telepon:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtPhone = new JTextField(15);
        txtPhone.setDocument(new NumericDocument(13));
        formPanel.add(txtPhone, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtEmail = new JTextField(15);
        txtEmail.setDocument(new EmailDocument()); // Terapkan custom document untuk hanya karakter valid email
        formPanel.add(txtEmail, gbc);

        // ❌ Status dihapus

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setOpaque(false);

        btnAdd = createStyledButton("Tambah", new Color(0x4CAF50));
        btnUpdate = createStyledButton("Update", new Color(0x2196F3));
        btnDelete = createStyledButton("Hapus", new Color(0xF44336));
        btnClear = createStyledButton("Clear", new Color(0x9E9E9E));

        btnAdd.addActionListener(e -> addDoctor());
        btnUpdate.addActionListener(e -> updateDoctor());
        btnDelete.addActionListener(e -> deleteDoctor());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 6; // diperbarui karena ada 1 field dihapus
        gbc.gridwidth = 2;
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

    JLabel tableTitle = new JLabel("Daftar Dokter");
    tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
    tableTitle.setForeground(new Color(0x6da395));

    // Tambahkan panel pencarian
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    searchPanel.setOpaque(false);
    searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    searchPanel.add(new JLabel("Cari Dokter:"));
    searchField = new JTextField(20);
    searchField.setToolTipText("Cari berdasarkan ID Dokter, Nama, Alamat, Telepon, atau Email");
    searchPanel.add(searchField);

    // Tambahkan panel judul dan pencarian ke bagian NORTH
    JPanel titleAndSearchPanel = new JPanel(new BorderLayout());
    titleAndSearchPanel.setOpaque(false);
    titleAndSearchPanel.add(tableTitle, BorderLayout.CENTER);
    titleAndSearchPanel.add(searchPanel, BorderLayout.EAST);

    tablePanel.add(titleAndSearchPanel, BorderLayout.NORTH);

    // ✅ Tambahkan label total dokter
    JLabel totalLabel = new JLabel("Total Dokter: 0", SwingConstants.RIGHT);
    totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    totalLabel.setForeground(new Color(0x6da395));
    tablePanel.add(totalLabel, BorderLayout.SOUTH);

    // Kolom tabel: Hanya ID, ID Dokter, Nama, Alamat, Telepon, Email
    String[] columns = {"ID", "ID Dokter", "Nama Lengkap", "Alamat", "Telepon", "Email"};
    tableModel = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    doctorTable = new JTable(tableModel);
    doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    doctorTable.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            loadSelectedDoctor();
        }
    });

    JTableHeader header = doctorTable.getTableHeader();
    header.setBackground(new Color(0x96A78D));
    header.setForeground(Color.WHITE);
    header.setFont(new Font("Segoe UI", Font.BOLD, 12));

    doctorTable.setRowHeight(25);
    doctorTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    doctorTable.setGridColor(new Color(200, 200, 200));
    doctorTable.setSelectionBackground(new Color(0x6da395));

    doctorTable.getColumnModel().getColumn(0).setMaxWidth(0);
    doctorTable.getColumnModel().getColumn(0).setMinWidth(0);
    doctorTable.getColumnModel().getColumn(0).setPreferredWidth(0);

    JScrollPane scrollPane = new JScrollPane(doctorTable);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);

    tablePanel.add(scrollPane, BorderLayout.CENTER);

    // ✅ Simpan referensi ke totalLabel agar bisa diupdate
    this.totalDoctorLabel = totalLabel; // tambahkan field di class: private JLabel totalDoctorLabel;

    return tablePanel;
}

    // ❌ loadSpecializations() dan applyFilter() dihapus

    private void setupTable() {
        // Buat dan tetapkan TableRowSorter
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> rowSorter =
            new javax.swing.table.TableRowSorter<>(tableModel);
        doctorTable.setRowSorter(rowSorter);

        doctorTable.getColumnModel().getColumn(0).setMinWidth(0);
        doctorTable.getColumnModel().getColumn(0).setMaxWidth(0);
        doctorTable.getColumnModel().getColumn(0).setPreferredWidth(0);

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

    private void addDoctor() {
        if (validateInput()) {
            try {
                Doctor doctor = new Doctor();
                doctor.setDoctorCode(generateNextDoctorCode());
                doctor.setFullName(txtFullName.getText().trim());
                // ❌ specialization dihapus
                doctor.setAddress(txtAddress.getText().trim());
                doctor.setPhone(txtPhone.getText().trim());
                doctor.setEmail(txtEmail.getText().trim());
                // ❌ status dihapus (default aktif atau tidak perlu)

                if (insertDoctor(doctor)) {
                    JOptionPane.showMessageDialog(this, "Data dokter berhasil ditambahkan!");
                    loadDoctorData(); // refresh data
                    clearForm();
                    if (dashboard != null) {
                        dashboard.refreshDashboard();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menambah data dokter!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateDoctor() {
        if (selectedDoctorId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dokter yang akan diupdate!");
            return;
        }

        if (validateInput()) {
            try {
                Doctor doctor = new Doctor();
                doctor.setId(selectedDoctorId);
                doctor.setDoctorCode(txtDoctorCode.getText().trim()); // pertahankan kode
                doctor.setFullName(txtFullName.getText().trim());
                // ❌ specialization dihapus
                doctor.setAddress(txtAddress.getText().trim());
                doctor.setPhone(txtPhone.getText().trim());
                doctor.setEmail(txtEmail.getText().trim());
                // ❌ status dihapus

                if (updateDoctorInDB(doctor)) {
                    JOptionPane.showMessageDialog(this, "Data dokter berhasil diupdate!");
                    loadDoctorData();
                    clearForm();
                    if (dashboard != null) {
                        dashboard.refreshDashboard();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengupdate data dokter!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteDoctor() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dokter yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus data dokter ini?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (Integer) tableModel.getValueAt(selectedRow, 0);
            if (deleteDoctorFromDB(id)) {
                JOptionPane.showMessageDialog(this, "Data dokter berhasil dihapus!");
                loadDoctorData();
                clearForm();
                if (dashboard != null) {
                    dashboard.refreshDashboard();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data dokter!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        txtDoctorCode.setText("");
        txtFullName.setText("");
        txtAddress.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        selectedDoctorId = -1;
        doctorTable.clearSelection();
    }

    private boolean validateInput() {
        if (txtFullName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Lengkap tidak boleh kosong!");
            txtFullName.requestFocus();
            return false;
        }
        // ❌ validasi spesialisasi dihapus
        if (txtAddress.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alamat tidak boleh kosong!");
            txtAddress.requestFocus();
            return false;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email tidak boleh kosong!");
            txtEmail.requestFocus();
            return false;
        }
        if (!EMAIL_PATTERN.matcher(txtEmail.getText().trim()).matches()) {
            JOptionPane.showMessageDialog(this, "Format email tidak valid!");
            txtEmail.requestFocus();
            return false;
        }

        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No. Telepon tidak boleh kosong!");
            txtPhone.requestFocus();
            return false;
        }
        if (!phone.matches("\\d{10,13}")) {
            JOptionPane.showMessageDialog(this, "No. Telepon harus 10–13 digit angka tanpa spasi atau simbol!");
            txtPhone.requestFocus();
            return false;
        }
        
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim().toLowerCase();

        if (isDuplicateDoctor(fullName, email, selectedDoctorId)) {
            JOptionPane.showMessageDialog(this, 
                "Dokter dengan nama dan email yang sama sudah ada!", 
                "Duplikat Data", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void loadSelectedDoctor() {
        int selectedViewRow = doctorTable.getSelectedRow();
        if (selectedViewRow >= 0) {
            // Konversi dari indeks tampilan ke indeks model sebenarnya
            int selectedModelRow = doctorTable.convertRowIndexToModel(selectedViewRow);

            selectedDoctorId = (Integer) tableModel.getValueAt(selectedModelRow, 0);
            txtDoctorCode.setText((String) tableModel.getValueAt(selectedModelRow, 1));
            txtFullName.setText((String) tableModel.getValueAt(selectedModelRow, 2));
            txtAddress.setText((String) tableModel.getValueAt(selectedModelRow, 3));
            txtPhone.setText((String) tableModel.getValueAt(selectedModelRow, 4));
            txtEmail.setText((String) tableModel.getValueAt(selectedModelRow, 5));
            // ❌ status tidak dimuat
        }
    }

    private void loadDoctorData() {
        // Simpan status filter saat ini
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> rowSorter
            = (javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel>) doctorTable.getRowSorter();
        javax.swing.RowFilter rowFilter = null;
        if (rowSorter != null) {
            rowFilter = rowSorter.getRowFilter();
        }

        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM doctors ORDER BY doctor_code";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("doctor_code"),
                    rs.getString("full_name"),
                    rs.getString("address"),
                    rs.getString("phone"),
                    rs.getString("email")
                };
                tableModel.addRow(row);
                count++;
            }

            // ✅ Update total dokter
            if (totalDoctorLabel != null) {
                totalDoctorLabel.setText("Total Dokter: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data dokter: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Setelah memuat data baru, kembalikan filter jika ada
        if (rowSorter != null) {
            rowSorter.setRowFilter(rowFilter);
        }
    }
    
    private boolean isDuplicateDoctor(String fullName, String email, int excludeId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM doctors WHERE LOWER(full_name) = LOWER(?) AND LOWER(email) = LOWER(?)";
            if (excludeId != -1) {
                sql += " AND id != ?";
            }
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            if (excludeId != -1) {
                pstmt.setInt(3, excludeId);
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saat cek duplikat: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    private boolean insertDoctor(Doctor doctor) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO doctors (doctor_code, full_name, address, phone, email) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, doctor.getDoctorCode());
            pstmt.setString(2, doctor.getFullName());
            pstmt.setString(3, doctor.getAddress());
            pstmt.setString(4, doctor.getPhone());
            pstmt.setString(5, doctor.getEmail());
            // ❌ specialization dan is_active dihapus

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updateDoctorInDB(Doctor doctor) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE doctors SET doctor_code=?, full_name=?, address=?, phone=?, email=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, doctor.getDoctorCode());
            pstmt.setString(2, doctor.getFullName());
            pstmt.setString(3, doctor.getAddress());
            pstmt.setString(4, doctor.getPhone());
            pstmt.setString(5, doctor.getEmail());
            pstmt.setInt(6, doctor.getId());
            // ❌ specialization dan is_active dihapus

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean deleteDoctorFromDB(int doctorId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM doctors WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private String generateNextDoctorCode() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT MAX(CAST(SUBSTRING(doctor_code, 4) AS SIGNED)) AS max_num FROM doctors WHERE doctor_code LIKE 'DOK%'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            int nextNumber = 1;
            if (rs.next()) {
                Integer maxNum = rs.getObject("max_num", Integer.class);
                if (maxNum != null) {
                    nextNumber = maxNum + 1;
                }
            }
            return String.format("DOK%03d", nextNumber);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal generate kode dokter: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return "DOK001";
        }
    }

    private void filterTable(String searchText) {
        // Konversi teks pencarian ke huruf kecil untuk pencarian case-insensitive
        String lowerSearchText = searchText.toLowerCase().trim();

        // Dapatkan TableRowSorter untuk tabel
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> rowSorter
            = (javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel>) doctorTable.getRowSorter();

        if (lowerSearchText.isEmpty()) {
            // Jika tidak ada teks pencarian, tampilkan semua data
            rowSorter.setRowFilter(null);
        } else {
            // Buat filter untuk mencocokkan teks di semua kolom (kecuali kolom ID yang tersembunyi)
            // Kolom ID diabaikan karena tidak ditampilkan ke pengguna
            rowSorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(lowerSearchText), 1, 2, 3, 4, 5)); // Kolom 1-5 (ID Dokter ke Email)
        }
    }
}