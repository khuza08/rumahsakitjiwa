package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.Room;

public class RoomCRUDPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable roomTable;
    private JTextField txtRoomNumber, txtRoomType, txtPrice;
    private JComboBox<String> cbStatus;
    private JComboBox<String> cbFilterStatus; // filter status
    private JTextArea txtDescription;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedRoomId = -1;
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private JLabel lblTotalRooms, lblAvailableRooms, lblOccupiedRooms, lblMaintenanceRooms;

    private Dashboard dashboard;

    public RoomCRUDPanel() {
        initComponents();
        setupTable();
        loadRoomData();
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

        JLabel titleLabel = new JLabel("Form Data Kamar");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0x6da395));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("No. Kamar:"), gbc);
        txtRoomNumber = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtRoomNumber, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Tipe Kamar:"), gbc);
        txtRoomType = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtRoomType, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Status:"), gbc);
        cbStatus = new JComboBox<>(new String[]{"Tersedia", "Terisi", "Maintenance"});
        gbc.gridx = 1;
        formPanel.add(cbStatus, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Harga/Hari:"), gbc);
        txtPrice = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtPrice, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Deskripsi:"), gbc);
        txtDescription = new JTextArea(3, 15);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtDescription);
        gbc.gridx = 1;
        formPanel.add(scrollPane, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setOpaque(false);
        btnAdd = createStyledButton("Tambah", new Color(0x4CAF50));
        btnUpdate = createStyledButton("Update", new Color(0x2196F3));
        btnDelete = createStyledButton("Hapus", new Color(0xF44336));
        btnClear = createStyledButton("Clear", new Color(0x9E9E9E));

        btnAdd.addActionListener(e -> addRoom());
        btnUpdate.addActionListener(e -> updateRoom());
        btnDelete.addActionListener(e -> deleteRoom());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 0;
        gbc.gridy = 6;
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
        JLabel tableTitle = new JLabel("Daftar Kamar");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(0x6da395));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "No. Kamar", "Tipe", "Status", "Harga", "Deskripsi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedRoom();
            }
        });

        JTableHeader header = roomTable.getTableHeader();
        header.setBackground(new Color(0x96A78D));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roomTable.setRowHeight(25);
        roomTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roomTable.setGridColor(new Color(200, 200, 200));
        roomTable.setSelectionBackground(new Color(0x6da395));

        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Panel filter dan label jumlah kamar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Filter Status:"));
        cbFilterStatus = new JComboBox<>(new String[]{"Semua", "Tersedia", "Terisi", "Maintenance"});
        cbFilterStatus.addActionListener(e -> loadRoomData());
        filterPanel.add(cbFilterStatus);

        lblTotalRooms = new JLabel("Total Kamar: 0");
        lblAvailableRooms = new JLabel("Tersedia: 0");
        lblOccupiedRooms = new JLabel("Terisi: 0");
        lblMaintenanceRooms = new JLabel("Maintenance: 0");

        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(lblTotalRooms);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(lblAvailableRooms);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(lblOccupiedRooms);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(lblMaintenanceRooms);

        tablePanel.add(filterPanel, BorderLayout.SOUTH);

        setupTable();

        return tablePanel;
    }

    private void setupTable() {
        roomTable.getColumnModel().getColumn(0).setMinWidth(0);
        roomTable.getColumnModel().getColumn(0).setMaxWidth(0);
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(0);
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

    private void addRoom() {
        if (validateInput()) {
            try {
                Room room = new Room();
                room.setRoomNumber(txtRoomNumber.getText().trim());
                room.setRoomType(txtRoomType.getText().trim());
                room.setStatus((String) cbStatus.getSelectedItem());
                room.setPrice(parseFromRupiah(txtPrice.getText().trim()));
                room.setDescription(txtDescription.getText().trim());
                if (insertRoom(room)) {
                    JOptionPane.showMessageDialog(this, "Data kamar berhasil ditambahkan!");
                    loadRoomData();
                    clearForm();
                    if (dashboard != null) dashboard.refreshDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menambah data kamar!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateRoom() {
        if (selectedRoomId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kamar yang akan diupdate!");
            return;
        }
        if (validateInput()) {
            try {
                Room room = new Room();
                room.setId(selectedRoomId);
                room.setRoomNumber(txtRoomNumber.getText().trim());
                room.setRoomType(txtRoomType.getText().trim());
                room.setStatus((String) cbStatus.getSelectedItem());
                room.setPrice(parseFromRupiah(txtPrice.getText().trim()));
                room.setDescription(txtDescription.getText().trim());
                if (updateRoomInDB(room)) {
                    JOptionPane.showMessageDialog(this, "Data kamar berhasil diupdate!");
                    loadRoomData();
                    clearForm();
                    if (dashboard != null) dashboard.refreshDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengupdate data kamar!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteRoom() {
        if (selectedRoomId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kamar yang akan dihapus!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data kamar ini?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (deleteRoomFromDB(selectedRoomId)) {
                JOptionPane.showMessageDialog(this, "Data kamar berhasil dihapus!");
                loadRoomData();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data kamar!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtRoomNumber.setText("");
        txtRoomType.setText("");
        cbStatus.setSelectedIndex(0);
        txtPrice.setText("");
        txtDescription.setText("");
        selectedRoomId = -1;
        roomTable.clearSelection();
    }

    private boolean validateInput() {
        if (txtRoomNumber.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No. Kamar tidak boleh kosong!");
            txtRoomNumber.requestFocus();
            return false;
        }
        if (txtRoomType.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tipe Kamar tidak boleh kosong!");
            txtRoomType.requestFocus();
            return false;
        }
        if (txtPrice.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harga tidak boleh kosong!");
            txtPrice.requestFocus();
            return false;
        }
        try {
            double price = parseFromRupiah(txtPrice.getText().trim());
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Harga harus lebih besar dari 0!");
                txtPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Format harga tidak valid! Gunakan angka tanpa titik/koma.");
            txtPrice.requestFocus();
            return false;
        }
        return true;
    }

    private void loadRoomData() {
        tableModel.setRowCount(0);
        String filter = (String) cbFilterStatus.getSelectedItem();
        int totalRooms = 0;
        int countAvailable = 0;
        int countOccupied = 0;
        int countMaintenance = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM rooms";
            if (filter != null && !filter.equals("Semua")) {
                sql += " WHERE status = ?";
            }
            sql += " ORDER BY room_number";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (filter != null && !filter.equals("Semua")) {
                pstmt.setString(1, filter);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                totalRooms++;
                String status = rs.getString("status");
                if ("Tersedia".equalsIgnoreCase(status)) countAvailable++;
                else if ("Terisi".equalsIgnoreCase(status)) countOccupied++;
                else if ("Maintenance".equalsIgnoreCase(status)) countMaintenance++;

                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        status,
                        rupiahFormat.format(rs.getDouble("price")),
                        rs.getString("description")
                };
                tableModel.addRow(row);
            }

            // Set label jumlah kamar
            lblTotalRooms.setText("Total Kamar: " + totalRooms);
            lblAvailableRooms.setText("Tersedia: " + countAvailable);
            lblOccupiedRooms.setText("Terisi: " + countOccupied);
            lblMaintenanceRooms.setText("Maintenance: " + countMaintenance);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data kamar: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedRoomId = (Integer) tableModel.getValueAt(selectedRow, 0);
            txtRoomNumber.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtRoomType.setText((String) tableModel.getValueAt(selectedRow, 2));
            cbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 3));
            String priceStr = (String) tableModel.getValueAt(selectedRow, 4);
            String cleanPrice = priceStr.replaceAll("[^\\d]", "");
            txtPrice.setText(cleanPrice);
            txtDescription.setText((String) tableModel.getValueAt(selectedRow, 5));
        }
    }

    private double parseFromRupiah(String rupiahString) {
        String cleanString = rupiahString.replaceAll("[^\\d]", "");
        try {
            return Double.parseDouble(cleanString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean insertRoom(Room room) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO rooms (room_number, room_type, status, price, description) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType());
            pstmt.setString(3, room.getStatus());
            pstmt.setDouble(4, room.getPrice());
            pstmt.setString(5, room.getDescription());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updateRoomInDB(Room room) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE rooms SET room_number=?, room_type=?, status=?, price=?, description=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType());
            pstmt.setString(3, room.getStatus());
            pstmt.setDouble(4, room.getPrice());
            pstmt.setString(5, room.getDescription());
            pstmt.setInt(6, room.getId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean deleteRoomFromDB(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM rooms WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
