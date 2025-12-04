/*
 * Resepsionis CRUD Panel
 * NIR: auto-generated R01, R02, ...
 * No. Tlp: 10-13 digits only
 */
package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import rumahsakitjiwa.database.DatabaseConnection;

public class ResepsionisCRUDPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable resepsionisTable;
    
    // Ganti JPasswordField menjadi JTextField agar teks terlihat
    private JTextField txtNIR, txtNama, txtAlamat, txtNoTlp, txtUsername, txtPassword;
    private JComboBox<String> cbGender;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    
    private int selectedId = -1;
    private Dashboard dashboard;
    private JTextField searchField;

    public ResepsionisCRUDPanel() {
        initComponents();
        setupTable();
        loadData();
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

        JLabel titleLabel = new JLabel("Form Data Resepsionis");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0x6da395));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("NIR:"), gbc);
        txtNIR = new JTextField(15);
        txtNIR.setEditable(false); // 🔒 Tidak bisa diubah manual
        txtNIR.setBackground(new Color(240, 240, 240)); // Greyed out look
        gbc.gridx = 1;
        formPanel.add(txtNIR, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Nama:"), gbc);
        txtNama = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtNama, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Alamat:"), gbc);
        txtAlamat = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtAlamat, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("No. Tlp:"), gbc);
        txtNoTlp = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtNoTlp, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Gender:"), gbc);
        cbGender = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
        gbc.gridx = 1;
        formPanel.add(cbGender, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Username:"), gbc);
        txtUsername = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Password:"), gbc);
        txtPassword = new JTextField(15); // Ganti JPasswordField menjadi JTextField
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setOpaque(false);
        btnAdd = createStyledButton("Tambah", new Color(0x4CAF50));
        btnUpdate = createStyledButton("Update", new Color(0x2196F3));
        btnDelete = createStyledButton("Hapus", new Color(0xF44336));
        btnClear = createStyledButton("Clear", new Color(0x9E9E9E));

        btnAdd.addActionListener(e -> addResepsionis());
        btnUpdate.addActionListener(e -> updateResepsionis());
        btnDelete.addActionListener(e -> deleteResepsionis());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(buttonPanel, gbc);

        // Generate NIR otomatis untuk form baru
        generateNextNIR();

        return formPanel;
    }

    // 🔢 Generate NIR berikutnya: R01, R02, ..., R100, dst.
    private void generateNextNIR() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT MAX(CAST(SUBSTRING(nir, 2) AS UNSIGNED)) as max_num FROM resepsionis WHERE nir LIKE 'R%'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            int nextNum = 1;
            if (rs.next()) {
                int max = rs.getInt("max_num");
                if (!rs.wasNull()) {
                    nextNum = max + 1;
                }
            }
            txtNIR.setText(String.format("R%02d", nextNum));
        } catch (SQLException e) {
            e.printStackTrace();
            txtNIR.setText("R01"); // fallback
        }
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

        JLabel tableTitle = new JLabel("Daftar Resepsionis");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(0x6da395));

        // Tambahkan panel pencarian
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        searchPanel.add(new JLabel("Cari Resepsionis:"));
        searchField = new JTextField(20);
        searchField.setToolTipText("Cari berdasarkan NIR, Nama, Alamat, atau Username");
        searchPanel.add(searchField);

        // Tambahkan panel judul dan pencarian ke bagian NORTH
        JPanel titleAndSearchPanel = new JPanel(new BorderLayout());
        titleAndSearchPanel.setOpaque(false);
        titleAndSearchPanel.add(tableTitle, BorderLayout.CENTER);
        titleAndSearchPanel.add(searchPanel, BorderLayout.EAST);

        tablePanel.add(titleAndSearchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "NIR", "Nama", "Alamat", "No. Tlp", "Gender", "Username", "Password"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resepsionisTable = new JTable(tableModel);
        resepsionisTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resepsionisTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedData();
            }
        });

        JTableHeader header = resepsionisTable.getTableHeader();
        header.setBackground(new Color(0x96A78D));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        resepsionisTable.setRowHeight(25);
        resepsionisTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resepsionisTable.setGridColor(new Color(200, 200, 200));
        resepsionisTable.setSelectionBackground(new Color(0x6da395));

        JScrollPane scrollPane = new JScrollPane(resepsionisTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        setupTable();
        return tablePanel;
    }

    private void setupTable() {
        // Buat dan tetapkan TableRowSorter
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> rowSorter =
            new javax.swing.table.TableRowSorter<>(tableModel);
        resepsionisTable.setRowSorter(rowSorter);

        resepsionisTable.getColumnModel().getColumn(0).setMinWidth(0);
        resepsionisTable.getColumnModel().getColumn(0).setMaxWidth(0);
        resepsionisTable.getColumnModel().getColumn(0).setPreferredWidth(0);

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
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
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

    // ✅ Validasi No. Tlp: 10-13 digit
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        // Hanya ambil digit
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 13;
    }

    private boolean validateInput() {
        if (txtNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong!");
            txtNama.requestFocus();
            return false;
        }
        if (txtNoTlp.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No. Tlp tidak boleh kosong!");
            txtNoTlp.requestFocus();
            return false;
        }
        if (!isValidPhoneNumber(txtNoTlp.getText().trim())) {
            JOptionPane.showMessageDialog(this, "No. Tlp harus 10-13 digit angka!");
            txtNoTlp.requestFocus();
            return false;
        }
        if (txtUsername.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username tidak boleh kosong!");
            txtUsername.requestFocus();
            return false;
        }
        // Ganti validasi password sesuai JTextField
        if (txtPassword.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password tidak boleh kosong!");
            txtPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void addResepsionis() {
        if (!validateInput()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO resepsionis (nir, nama, alamat, no_tlp, gender, username, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtNIR.getText().trim());
            pstmt.setString(2, txtNama.getText().trim());
            pstmt.setString(3, txtAlamat.getText().trim());
            // Simpan no_tlp sebagai digit saja (opsional)
            pstmt.setString(4, txtNoTlp.getText().trim().replaceAll("[^0-9]", ""));
            pstmt.setString(5, (String) cbGender.getSelectedItem());
            pstmt.setString(6, txtUsername.getText().trim());
            // Ganti cara mengambil password sesuai JTextField
            pstmt.setString(7, txtPassword.getText().trim());

            if (pstmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Data resepsionis berhasil ditambahkan!");
                loadData();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambah data!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateResepsionis() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih resepsionis yang akan diupdate!");
            return;
        }
        if (!validateInput()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE resepsionis SET nama=?, alamat=?, no_tlp=?, gender=?, username=?, password=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtNama.getText().trim());
            pstmt.setString(2, txtAlamat.getText().trim());
            pstmt.setString(3, txtNoTlp.getText().trim().replaceAll("[^0-9]", ""));
            pstmt.setString(4, (String) cbGender.getSelectedItem());
            pstmt.setString(5, txtUsername.getText().trim());
            // Ganti cara mengambil password sesuai JTextField
            pstmt.setString(6, txtPassword.getText().trim());
            pstmt.setInt(7, selectedId);

            if (pstmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
                loadData();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengupdate data!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteResepsionis() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih resepsionis yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin hapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM resepsionis WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedId);

            if (pstmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                loadData();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        generateNextNIR(); // Auto-generate NIR baru
        txtNama.setText("");
        txtAlamat.setText("");
        txtNoTlp.setText("");
        cbGender.setSelectedIndex(0);
        txtUsername.setText("");
        txtPassword.setText("");
        selectedId = -1;
        resepsionisTable.clearSelection();
    }

    private void loadData() {
        // Simpan status filter saat ini
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> rowSorter
            = (javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel>) resepsionisTable.getRowSorter();
        javax.swing.RowFilter rowFilter = null;
        if (rowSorter != null) {
            rowFilter = rowSorter.getRowFilter();
        }

        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM resepsionis ORDER BY nir";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nir"),
                    rs.getString("nama"),
                    rs.getString("alamat"),
                    rs.getString("no_tlp"),
                    rs.getString("gender"),
                    rs.getString("username"),
                    rs.getString("password")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Setelah memuat data baru, kembalikan filter jika ada
        if (rowSorter != null) {
            rowSorter.setRowFilter(rowFilter);
        }
    }

    private void loadSelectedData() {
        int selectedViewRow = resepsionisTable.getSelectedRow();
        if (selectedViewRow >= 0) {
            // Konversi dari indeks tampilan ke indeks model sebenarnya
            int selectedModelRow = resepsionisTable.convertRowIndexToModel(selectedViewRow);

            selectedId = (Integer) tableModel.getValueAt(selectedModelRow, 0);
            txtNIR.setText((String) tableModel.getValueAt(selectedModelRow, 1));
            txtNama.setText((String) tableModel.getValueAt(selectedModelRow, 2));
            txtAlamat.setText((String) tableModel.getValueAt(selectedModelRow, 3));
            txtNoTlp.setText((String) tableModel.getValueAt(selectedModelRow, 4));
            cbGender.setSelectedItem(tableModel.getValueAt(selectedModelRow, 5));
            txtUsername.setText((String) tableModel.getValueAt(selectedModelRow, 6));
            txtPassword.setText((String) tableModel.getValueAt(selectedModelRow, 7));
        }
    }

    private void filterTable(String searchText) {
        // Konversi teks pencarian ke huruf kecil untuk pencarian case-insensitive
        String lowerSearchText = searchText.toLowerCase().trim();

        // Dapatkan TableRowSorter untuk tabel
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> rowSorter
            = (javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel>) resepsionisTable.getRowSorter();

        if (lowerSearchText.isEmpty()) {
            // Jika tidak ada teks pencarian, tampilkan semua data
            rowSorter.setRowFilter(null);
        } else {
            // Buat filter untuk mencocokkan teks di semua kolom (kecuali kolom ID yang tersembunyi)
            // Kolom ID diabaikan karena tidak ditampilkan ke pengguna
            rowSorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(lowerSearchText), 1, 2, 3, 4, 5, 6, 7)); // Kolom 1-7 (NIR ke atas)
        }
    }
}
