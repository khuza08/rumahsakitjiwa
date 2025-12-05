package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import rumahsakitjiwa.database.DatabaseConnection;
import rumahsakitjiwa.model.RoomBooking;
import rumahsakitjiwa.model.Pasien;
import rumahsakitjiwa.model.Room;
import com.toedter.calendar.JCalendar;

public class RoomBookingPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable bookingTable;
    private JTextField txtBookingId, txtCheckInDate, txtCheckOutDate, txtNotes;
    private JComboBox<String> cbRoomType, cbStatus, cbPatient, cbRoom;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedBookingId = -1;
    private Dashboard dashboard;
    private String userRole;

    public RoomBookingPanel() {
        initComponents();
        setupTable();
        loadBookings();
    }

    public void setUserRole(String role) {
        this.userRole = role;
        applyRolePermissions();
    }

    private void applyRolePermissions() {
        boolean isResepsionis = "resepsionis".equalsIgnoreCase(userRole);

        // Set combobox and field editability
        cbPatient.setEnabled(isResepsionis);
        cbRoom.setEnabled(isResepsionis);
        txtCheckInDate.setEditable(isResepsionis);
        txtCheckOutDate.setEditable(isResepsionis);
        txtNotes.setEditable(isResepsionis);
        cbRoomType.setEnabled(isResepsionis);
        cbStatus.setEnabled(isResepsionis && "admin".equalsIgnoreCase(userRole)); // Only admin can change status

        // Set button visibility
        btnAdd.setVisible(isResepsionis);
        btnUpdate.setVisible(isResepsionis);
        btnDelete.setVisible("admin".equalsIgnoreCase(userRole));
        btnClear.setVisible(isResepsionis);
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
        formPanel.setPreferredSize(new Dimension(400, 0));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Form Booking Kamar Rawat Inap");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0x6da395));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Booking ID
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("ID Booking:"), gbc);
        txtBookingId = new JTextField(15);
        txtBookingId.setEditable(false);
        txtBookingId.setBackground(Color.LIGHT_GRAY);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtBookingId, gbc);

        // Patient Selection
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Pilih Pasien:"), gbc);
        cbPatient = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbPatient, gbc);

        // Load patients into the combobox
        loadPatientsToComboBox();
        cbPatient.setSelectedIndex(-1); // Tidak ada pilihan yang dipilih secara default

        // Room Selection
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Pilih Kamar:"), gbc);
        cbRoom = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbRoom, gbc);

        // Load available rooms into the combobox
        loadAvailableRoomsToComboBox();
        cbRoom.setSelectedIndex(-1); // Tidak ada pilihan yang dipilih secara default

        // Room Type
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tipe Kamar:"), gbc);
        cbRoomType = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbRoomType, gbc);

        // Check-in Date
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tanggal Check-in:"), gbc);

        JPanel checkInPanel = new JPanel(new BorderLayout(5, 0));
        txtCheckInDate = new JTextField(10);
        JButton btnCheckInDate = new JButton("📅");
        btnCheckInDate.setPreferredSize(new Dimension(30, txtCheckInDate.getPreferredSize().height));
        btnCheckInDate.addActionListener(e -> showDatePicker(txtCheckInDate, "Pilih Tanggal Check-in"));

        checkInPanel.add(txtCheckInDate, BorderLayout.CENTER);
        checkInPanel.add(btnCheckInDate, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(checkInPanel, gbc);

        // Check-out Date
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tanggal Check-out:"), gbc);

        JPanel checkOutPanel = new JPanel(new BorderLayout(5, 0));
        txtCheckOutDate = new JTextField(10);
        JButton btnCheckOutDate = new JButton("📅");
        btnCheckOutDate.setPreferredSize(new Dimension(30, txtCheckOutDate.getPreferredSize().height));
        btnCheckOutDate.addActionListener(e -> showDatePicker(txtCheckOutDate, "Pilih Tanggal Check-out"));

        checkOutPanel.add(txtCheckOutDate, BorderLayout.CENTER);
        checkOutPanel.add(btnCheckOutDate, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(checkOutPanel, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Status:"), gbc);
        cbStatus = new JComboBox<>(new String[]{"Booking", "Check-in", "Check-out", "Cancelled"});
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbStatus, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Catatan:"), gbc);
        txtNotes = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtNotes, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setOpaque(false);

        btnAdd = createStyledButton("Booking", new Color(0x4CAF50));
        btnUpdate = createStyledButton("Update", new Color(0x2196F3));
        btnDelete = createStyledButton("Hapus", new Color(0xF44336));
        btnClear = createStyledButton("Clear", new Color(0x9E9E9E));

        btnAdd.addActionListener(e -> addBooking());
        btnUpdate.addActionListener(e -> updateBooking());
        btnDelete.addActionListener(e -> deleteBooking());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(buttonPanel, gbc);

        loadRoomTypes();

        // Tambahkan listener untuk combobox pasien dan kamar
        cbPatient.addActionListener(e -> updatePatientInfo());
        cbRoom.addActionListener(e -> updateRoomInfo());

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

        JLabel tableTitle = new JLabel("Daftar Booking Kamar");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(0x6da395));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "Kode Pasien", "Nama Pasien", "No. Kamar", "Tipe Kamar", "Check-in", "Check-out", "Status", "Tanggal Booking"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingTable = new JTable(tableModel);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedBooking();
            }
        });

        JTableHeader header = bookingTable.getTableHeader();
        header.setBackground(new Color(0x96A78D));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        bookingTable.setRowHeight(25);
        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bookingTable.setGridColor(new Color(200, 200, 200));
        bookingTable.setSelectionBackground(new Color(0x6da395));

        // Hide ID column
        bookingTable.getColumnModel().getColumn(0).setMinWidth(0);
        bookingTable.getColumnModel().getColumn(0).setMaxWidth(0);
        bookingTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private void setupTable() {
        bookingTable.getColumnModel().getColumn(0).setMinWidth(0);
        bookingTable.getColumnModel().getColumn(0).setMaxWidth(0);
        bookingTable.getColumnModel().getColumn(0).setPreferredWidth(0);
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

    private void loadRoomTypes() {
        cbRoomType.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT DISTINCT room_type FROM rooms ORDER BY room_type";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cbRoomType.addItem(rs.getString("room_type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPatientsToComboBox() {
        cbPatient.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT patient_code, full_name FROM patients ORDER BY patient_code";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String patientInfo = rs.getString("patient_code") + " - " + rs.getString("full_name");
                cbPatient.addItem(patientInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAvailableRoomsToComboBox() {
        cbRoom.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT room_number, room_type FROM rooms WHERE status = 'Tersedia' ORDER BY room_number";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String roomInfo = rs.getString("room_number") + " (" + rs.getString("room_type") + ")";
                cbRoom.addItem(roomInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePatientInfo() {
        String selectedPatient = (String) cbPatient.getSelectedItem();
        if (selectedPatient != null && !selectedPatient.isEmpty()) {
            String[] parts = selectedPatient.split(" - ", 2);
            if (parts.length >= 2) {
                String patientCode = parts[0].trim();
                // Tidak perlu mengupdate field karena kita menggunakan kode yang terambil dari combobox
            }
        }
    }

    private void updateRoomInfo() {
        String selectedRoom = (String) cbRoom.getSelectedItem();
        if (selectedRoom != null && !selectedRoom.isEmpty()) {
            // Ekstrak room number dari format "room_number (room_type)"
            int openParenIndex = selectedRoom.indexOf('(');
            if (openParenIndex > 0) {
                // Ekstrak room type dari dalam tanda kurung
                String roomType = selectedRoom.substring(openParenIndex + 1, selectedRoom.length() - 1);
                cbRoomType.setSelectedItem(roomType);
            }
        }
    }

    private void addBooking() {
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        if (!validateInput()) return;

        try {
            RoomBooking booking = new RoomBooking();

            // Ekstrak kode pasien dan nama dari combobox
            String selectedPatient = (String) cbPatient.getSelectedItem();
            String patientCode = "";
            String patientName = "";
            if (selectedPatient != null && !selectedPatient.isEmpty()) {
                String[] parts = selectedPatient.split(" - ", 2);
                if (parts.length >= 2) {
                    patientCode = parts[0].trim();
                    patientName = parts[1].trim();
                }
            }

            // Ekstrak nomor kamar dari combobox
            String selectedRoom = (String) cbRoom.getSelectedItem();
            String roomNumber = "";
            if (selectedRoom != null && !selectedRoom.isEmpty()) {
                int endIndex = selectedRoom.indexOf(' ');
                if (endIndex > 0) {
                    roomNumber = selectedRoom.substring(0, endIndex);
                } else {
                    roomNumber = selectedRoom;
                }
            }

            booking.setPatientCode(patientCode);
            booking.setPatientName(patientName);
            booking.setRoomNumber(roomNumber);
            booking.setRoomType((String) cbRoomType.getSelectedItem());
            java.sql.Timestamp checkInDate = java.sql.Timestamp.valueOf(LocalDate.parse(txtCheckInDate.getText().trim()).atStartOfDay());
            java.sql.Timestamp checkOutDate = java.sql.Timestamp.valueOf(LocalDate.parse(txtCheckOutDate.getText().trim()).atStartOfDay());
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setStatus((String) cbStatus.getSelectedItem());
            booking.setNotes(txtNotes.getText().trim());

            if (insertBooking(booking)) {
                JOptionPane.showMessageDialog(this, "Booking kamar berhasil ditambahkan!");
                loadBookings();
                clearForm();
                loadAvailableRoomsToComboBox(); // Refresh daftar kamar tersedia
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambah booking kamar!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBooking() {
        if (!"resepsionis".equalsIgnoreCase(userRole)) return;

        if (selectedBookingId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih booking yang akan diupdate!");
            return;
        }

        if (!validateInput()) return;

        try {
            RoomBooking booking = new RoomBooking();
            booking.setId(selectedBookingId);

            // Ekstrak kode pasien dan nama dari combobox
            String selectedPatient = (String) cbPatient.getSelectedItem();
            String patientCode = "";
            String patientName = "";
            if (selectedPatient != null && !selectedPatient.isEmpty()) {
                String[] parts = selectedPatient.split(" - ", 2);
                if (parts.length >= 2) {
                    patientCode = parts[0].trim();
                    patientName = parts[1].trim();
                }
            }

            // Ekstrak nomor kamar dari combobox
            String selectedRoom = (String) cbRoom.getSelectedItem();
            String roomNumber = "";
            if (selectedRoom != null && !selectedRoom.isEmpty()) {
                int endIndex = selectedRoom.indexOf(' ');
                if (endIndex > 0) {
                    roomNumber = selectedRoom.substring(0, endIndex);
                } else {
                    roomNumber = selectedRoom;
                }
            }

            booking.setPatientCode(patientCode);
            booking.setPatientName(patientName);
            booking.setRoomNumber(roomNumber);
            booking.setRoomType((String) cbRoomType.getSelectedItem());
            java.sql.Timestamp checkInDate = java.sql.Timestamp.valueOf(LocalDate.parse(txtCheckInDate.getText().trim()).atStartOfDay());
            java.sql.Timestamp checkOutDate = java.sql.Timestamp.valueOf(LocalDate.parse(txtCheckOutDate.getText().trim()).atStartOfDay());
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setStatus((String) cbStatus.getSelectedItem());
            booking.setNotes(txtNotes.getText().trim());

            if (updateBookingInDB(booking)) {
                JOptionPane.showMessageDialog(this, "Booking kamar berhasil diupdate!");
                loadBookings();
                clearForm();
                loadAvailableRoomsToComboBox(); // Refresh daftar kamar tersedia
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengupdate booking kamar!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBooking() {
        if (!"admin".equalsIgnoreCase(userRole)) return;

        if (selectedBookingId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih booking yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus booking ini?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (deleteBookingFromDB(selectedBookingId)) {
                JOptionPane.showMessageDialog(this, "Booking kamar berhasil dihapus!");
                loadBookings();
                clearForm();
                if (dashboard != null) dashboard.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus booking kamar!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtBookingId.setText("");
        cbPatient.setSelectedIndex(-1);
        cbRoom.setSelectedIndex(-1);
        cbRoomType.setSelectedIndex(-1);
        txtCheckInDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        txtCheckOutDate.setText(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))); // Default 1 hari setelah check-in
        cbStatus.setSelectedItem("Booking");
        txtNotes.setText("");
        selectedBookingId = -1;
        bookingTable.clearSelection();
    }

    private boolean validateInput() {
        if (cbPatient.getSelectedItem() == null || cbPatient.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pasien harus dipilih!");
            cbPatient.requestFocus();
            return false;
        }

        if (cbRoom.getSelectedItem() == null || cbRoom.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kamar harus dipilih!");
            cbRoom.requestFocus();
            return false;
        }

        if (cbRoomType.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Tipe kamar harus dipilih!");
            cbRoomType.requestFocus();
            return false;
        }

        try {
            LocalDate checkIn = LocalDate.parse(txtCheckInDate.getText().trim());
            LocalDate checkOut = LocalDate.parse(txtCheckOutDate.getText().trim());
            LocalDate now = LocalDate.now();

            if (checkIn.isAfter(checkOut) || checkIn.isBefore(now)) {
                JOptionPane.showMessageDialog(this, "Tanggal check-in tidak valid (harus sekarang atau di masa depan dan sebelum check-out)!");
                return false;
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid! Gunakan format YYYY-MM-DD");
            return false;
        }

        return true;
    }

    private void loadSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedBookingId = (Integer) tableModel.getValueAt(selectedRow, 0);
            txtBookingId.setText(String.valueOf(selectedBookingId));

            // Set combobox pasien
            String patientCode = (String) tableModel.getValueAt(selectedRow, 1);
            String patientName = (String) tableModel.getValueAt(selectedRow, 2);
            String patientInfo = patientCode + " - " + patientName;
            cbPatient.setSelectedItem(patientInfo);

            // Set combobox kamar
            String roomNumber = (String) tableModel.getValueAt(selectedRow, 3);
            String roomType = (String) tableModel.getValueAt(selectedRow, 4);
            String roomInfo = roomNumber + " (" + roomType + ")";
            cbRoom.setSelectedItem(roomInfo);

            cbRoomType.setSelectedItem(roomType);
            try {
                String checkInDateStr = tableModel.getValueAt(selectedRow, 5).toString();
                String checkOutDateStr = tableModel.getValueAt(selectedRow, 6).toString();

                // Parse tanggal dari string ke format LocalDate
                LocalDate checkIn = LocalDate.parse(checkInDateStr.substring(0, 10));
                LocalDate checkOut = LocalDate.parse(checkOutDateStr.substring(0, 10));

                txtCheckInDate.setText(checkIn.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                txtCheckOutDate.setText(checkOut.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception e) {
                // Jika terjadi error parsing, gunakan tanggal saat ini
                txtCheckInDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                txtCheckOutDate.setText(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            cbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 7));
        }
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM room_bookings ORDER BY booking_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("patient_code"),
                    rs.getString("patient_name"),
                    rs.getString("room_number"),
                    rs.getString("room_type"),
                    rs.getTimestamp("check_in_date"),
                    rs.getTimestamp("check_out_date"),
                    rs.getString("status"),
                    rs.getTimestamp("booking_date")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data booking: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean insertBooking(RoomBooking booking) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO room_bookings (patient_code, patient_name, room_number, room_type, check_in_date, check_out_date, status, notes, booking_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, booking.getPatientCode());
            pstmt.setString(2, booking.getPatientName());
            pstmt.setString(3, booking.getRoomNumber());
            pstmt.setString(4, booking.getRoomType());
            pstmt.setTimestamp(5, booking.getCheckInDate());
            pstmt.setTimestamp(6, booking.getCheckOutDate());
            pstmt.setString(7, booking.getStatus());
            pstmt.setString(8, booking.getNotes());
            pstmt.setTimestamp(9, new Timestamp(System.currentTimeMillis()));

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean updateBookingInDB(RoomBooking booking) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE room_bookings SET patient_code=?, patient_name=?, room_number=?, room_type=?, check_in_date=?, check_out_date=?, status=?, notes=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, booking.getPatientCode());
            pstmt.setString(2, booking.getPatientName());
            pstmt.setString(3, booking.getRoomNumber());
            pstmt.setString(4, booking.getRoomType());
            pstmt.setTimestamp(5, booking.getCheckInDate());
            pstmt.setTimestamp(6, booking.getCheckOutDate());
            pstmt.setString(7, booking.getStatus());
            pstmt.setString(8, booking.getNotes());
            pstmt.setInt(9, booking.getId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean deleteBookingFromDB(int bookingId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM room_bookings WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookingId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showDatePicker(JTextField dateField, String title) {
        // Membuat dialog untuk pemilihan tanggal
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout());

        // Membuat JCalendar
        com.toedter.calendar.JCalendar calendar = new com.toedter.calendar.JCalendar();

        // Jika field sudah memiliki tanggal, atur ke kalender
        if (dateField.getText() != null && !dateField.getText().trim().isEmpty()) {
            try {
                java.util.Date selectedDate = java.text.SimpleDateFormat.getDateInstance().parse(dateField.getText());
                calendar.setDate(selectedDate);
            } catch (Exception e) {
                // Jika format tidak valid, gunakan tanggal saat ini
                calendar.setDate(new java.util.Date());
            }
        }

        // Tombol OK
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            java.util.Date selectedDate = calendar.getDate();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            dateField.setText(sdf.format(selectedDate));
            dialog.dispose();
        });

        // Tombol Batal
        JButton cancelButton = new JButton("Batal");
        cancelButton.addActionListener(e -> dialog.dispose());

        // Panel tombol
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.add(calendar, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}