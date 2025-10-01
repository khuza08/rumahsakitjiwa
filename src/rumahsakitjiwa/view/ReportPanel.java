package rumahsakitjiwa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import rumahsakitjiwa.database.DatabaseConnection;

public class ReportPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private NumberFormat integerFormat; // Deklarasi

    public ReportPanel() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        integerFormat = NumberFormat.getIntegerInstance(new Locale("id", "ID")); // Inisialisasi
        integerFormat.setGroupingUsed(false); // Setelah diinisialisasi
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Tabbed Pane for different reports
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(Color.WHITE);
        
        // Add tabs
        tabbedPane.addTab("Laporan Keuangan", createFinancialReportPanel());
        tabbedPane.addTab("Laporan Harian Pasien", createDailyPatientReportPanel());
        tabbedPane.addTab("Laporan Rawat Inap", createInpatientReportPanel());
        tabbedPane.addTab("Laporan Bulanan", createMonthlyReportPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    // ==================== LAPORAN KEUANGAN ====================
    private JPanel createFinancialReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setPreferredSize(new Dimension(getWidth(), 64)); // Tetapkan tinggi filter

        
        JComboBox<String> periodCombo = new JComboBox<>(new String[]{"Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini", "Custom"});
        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("Export PDF");
        
        filterPanel.add(new JLabel("Periode:"));
        filterPanel.add(periodCombo);
        filterPanel.add(refreshBtn);
        filterPanel.add(exportBtn);
        
        // Summary Panel
        JPanel summaryPanel = createFinancialSummaryPanel();
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        
        String[] columns = {"Tanggal", "Kode Pasien", "Nama Pasien", "Biaya Pemeriksaan", "Biaya Obat", "Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.setPreferredSize(new Dimension(getWidth(), 300)); // Tetapkan tinggi scroll

        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Load data
        refreshBtn.addActionListener(e -> loadFinancialData(model, periodCombo.getSelectedItem().toString(), summaryPanel));
        exportBtn.addActionListener(e -> exportFinancialReport());
        
        // Initial load
        loadFinancialData(model, "Hari Ini", summaryPanel);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(summaryPanel, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFinancialSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.setPreferredSize(new Dimension(getWidth(), 150)); // Tetapkan tinggi summary
        
        panel.add(createSummaryCard("Total Transaksi", "0", new Color(0x2196F3)));
        panel.add(createSummaryCard("Total Pendapatan", "Rp 0", new Color(0x4CAF50)));
        panel.add(createSummaryCard("Biaya Pemeriksaan", "Rp 0", new Color(0xFF9800)));
        panel.add(createSummaryCard("Biaya Obat", "Rp 0", new Color(0x9C27B0)));
        panel.add(createSummaryCard("Rata-rata per Pasien", "Rp 0", new Color(0x00BCD4)));
        panel.add(createSummaryCard("Pasien Darurat", "0", new Color(0xF44336)));
        
        return panel;
    }
    
    private void loadFinancialData(DefaultTableModel model, String period, JPanel summaryPanel) {
        model.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String dateCondition = getDateCondition(period);
            String sql = "SELECT p.created_at, p.patient_code, p.full_name, " +
                        "p.examination_fee, p.medicine_fee, " +
                        "(p.examination_fee + p.medicine_fee) as total, " +
                        "p.emergency_status " +
                        "FROM patients p WHERE " + dateCondition + " ORDER BY p.created_at DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            int totalTransactions = 0;
            double totalRevenue = 0;
            double totalExamination = 0;
            double totalMedicine = 0;
            int emergencyCount = 0;
            
            while (rs.next()) {
                String date = dateFormat.format(rs.getTimestamp("created_at"));
                String patientCode = rs.getString("patient_code");
                String fullName = rs.getString("full_name");
                double examFee = rs.getDouble("examination_fee");
                double medFee = rs.getDouble("medicine_fee");
                double total = rs.getDouble("total");
                boolean emergencyStatus = rs.getBoolean("emergency_status");
                
                model.addRow(new Object[]{
                    date, patientCode, fullName,
                    currencyFormat.format(examFee),
                    currencyFormat.format(medFee),
                    currencyFormat.format(total)
                });
                
                totalTransactions++;
                totalRevenue += total;
                totalExamination += examFee;
                totalMedicine += medFee;
                
                if (emergencyStatus) emergencyCount++;
            }
            
            // Update summary
            double avgPerPatient = totalTransactions > 0 ? totalRevenue / totalTransactions : 0;
            updateSummaryCard(summaryPanel, 0, String.valueOf(totalTransactions));
            updateSummaryCard(summaryPanel, 1, currencyFormat.format(totalRevenue));
            updateSummaryCard(summaryPanel, 2, currencyFormat.format(totalExamination));
            updateSummaryCard(summaryPanel, 3, currencyFormat.format(totalMedicine));
            updateSummaryCard(summaryPanel, 4, currencyFormat.format(avgPerPatient));
            updateSummaryCard(summaryPanel, 5, String.valueOf(emergencyCount));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading financial data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ==================== LAPORAN HARIAN PASIEN ====================
    private JPanel createDailyPatientReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Date Selector
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());
        
        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("Export PDF");
        
        filterPanel.add(new JLabel("Tanggal:"));
        filterPanel.add(dateSpinner);
        filterPanel.add(refreshBtn);
        filterPanel.add(exportBtn);
        
        // Summary Cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        summaryPanel.add(createSummaryCard("Pasien Masuk", "0", new Color(0x4CAF50)));
        summaryPanel.add(createSummaryCard("Pasien Keluar", "0", new Color(0x2196F3)));
        summaryPanel.add(createSummaryCard("Status Darurat", "0", new Color(0xF44336)));
        summaryPanel.add(createSummaryCard("Total Aktif", "0", new Color(0xFF9800)));
        
        // Table
        String[] columns = {"Waktu", "Kode Pasien", "Nama Pasien", "Usia", "Status Darurat", "Keterangan"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        refreshBtn.addActionListener(e -> loadDailyPatientData(model, (Date) dateSpinner.getValue(), summaryPanel));
        exportBtn.addActionListener(e -> exportDailyPatientReport((Date) dateSpinner.getValue()));
        
        // Initial load
        loadDailyPatientData(model, new Date(), summaryPanel);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(summaryPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadDailyPatientData(DefaultTableModel model, Date selectedDate, JPanel summaryPanel) {
        model.setRowCount(0);
        
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dayFormat.format(selectedDate);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT created_at, patient_code, full_name, birth_date, " +
                        "emergency_status, current_room_id FROM patients " +
                        "WHERE DATE(created_at) = ? ORDER BY created_at DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, dateStr);
            ResultSet rs = pstmt.executeQuery();
            
            int masuk = 0, keluar = 0, darurat = 0, aktif = 0;
            
            while (rs.next()) {
                String time = new SimpleDateFormat("HH:mm").format(rs.getTimestamp("created_at"));
                String patientCode = rs.getString("patient_code");
                String fullName = rs.getString("full_name");
                Date birthDate = rs.getDate("birth_date");
                boolean emergency = rs.getBoolean("emergency_status");
                String roomId = rs.getString("current_room_id");
                
                // Calculate age
                int age = calculateAge(birthDate);
                
                String keterangan = emergency ? "DARURAT" : "Normal";
                
                model.addRow(new Object[]{time, patientCode, fullName, age, emergency ? "Ya" : "Tidak", keterangan});
                
                masuk++;
                if (roomId != null && !roomId.isEmpty()) {
                    aktif++;
                } else {
                    keluar++;
                }
                
                if (emergency) darurat++;
            }
            
            // Update summary cards
            updateSummaryCard(summaryPanel, 0, String.valueOf(masuk));
            updateSummaryCard(summaryPanel, 1, String.valueOf(keluar));
            updateSummaryCard(summaryPanel, 2, String.valueOf(darurat));
            updateSummaryCard(summaryPanel, 3, String.valueOf(aktif));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading daily patient data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ==================== LAPORAN RAWAT INAP ====================
    private JPanel createInpatientReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Semua", "Aktif", "Keluar"});
        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("Export PDF");
        
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusCombo);
        filterPanel.add(refreshBtn);
        filterPanel.add(exportBtn);
        
        // Summary
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        summaryPanel.add(createSummaryCard("Total Rawat Inap", "0", new Color(0x2196F3)));
        summaryPanel.add(createSummaryCard("Rata-rata Lama Rawat", "0 hari", new Color(0x4CAF50)));
        summaryPanel.add(createSummaryCard("Terlama", "0 hari", new Color(0xFF9800)));
        summaryPanel.add(createSummaryCard("Tercepat", "0 hari", new Color(0x9C27B0)));
        
        // Table
        String[] columns = {"Kode Pasien", "Nama Pasien", "Tanggal Masuk", "Tanggal Keluar", "Lama Rawat", "No. Kamar", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        refreshBtn.addActionListener(e -> loadInpatientData(model, statusCombo.getSelectedItem().toString(), summaryPanel));
        exportBtn.addActionListener(e -> exportInpatientReport());
        
        // Initial load
        loadInpatientData(model, "Semua", summaryPanel);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(summaryPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadInpatientData(DefaultTableModel model, String statusFilter, JPanel summaryPanel) {
        model.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.patient_code, p.full_name, p.admission_date, " +
                        "p.discharge_date, p.current_room_id " +
                        "FROM patients p WHERE p.current_room_id IS NOT NULL";
            
            if (!"Semua".equals(statusFilter)) {
                if ("Aktif".equals(statusFilter)) {
                    sql += " AND p.discharge_date IS NULL";
                } else if ("Keluar".equals(statusFilter)) {
                    sql += " AND p.discharge_date IS NOT NULL";
                }
            }
            sql += " ORDER BY p.admission_date DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            int totalPatients = 0;
            long totalDays = 0;
            long maxDays = 0;
            long minDays = Long.MAX_VALUE;
            
            while (rs.next()) {
                String patientCode = rs.getString("patient_code");
                String fullName = rs.getString("full_name");
                Date admitDate = rs.getDate("admission_date");
                Date dischargeDate = rs.getDate("discharge_date");
                String roomNo = rs.getString("current_room_id");
                
                long days = 0;
                String dischargeStr = "-";
                String status = "Aktif";
                
                if (dischargeDate != null) {
                    days = (dischargeDate.getTime() - admitDate.getTime()) / (1000 * 60 * 60 * 24);
                    dischargeStr = new SimpleDateFormat("dd/MM/yyyy").format(dischargeDate);
                    status = "Keluar";
                } else {
                    days = (System.currentTimeMillis() - admitDate.getTime()) / (1000 * 60 * 60 * 24);
                }
                
                model.addRow(new Object[]{
                    patientCode, fullName,
                    new SimpleDateFormat("dd/MM/yyyy").format(admitDate),
                    dischargeStr,
                    days + " hari",
                    roomNo,
                    status
                });
                
                totalPatients++;
                totalDays += days;
                if (days > maxDays) maxDays = days;
                if (days < minDays) minDays = days;
            }
            
            // Update summary
            double avgDays = totalPatients > 0 ? (double) totalDays / totalPatients : 0;
            updateSummaryCard(summaryPanel, 0, String.valueOf(totalPatients));
            updateSummaryCard(summaryPanel, 1, String.format("%.1f hari", avgDays));
            updateSummaryCard(summaryPanel, 2, maxDays + " hari");
            updateSummaryCard(summaryPanel, 3, (minDays == Long.MAX_VALUE ? 0 : minDays) + " hari");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading inpatient data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ==================== LAPORAN BULANAN ====================
    private JPanel createMonthlyReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        
        String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                          "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedIndex(new Date().getMonth());
        
        // Gunakan tahun saat ini
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(currentYear, currentYear - 5, currentYear + 5, 1));

        // Format spinner untuk menampilkan tanpa desimal
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(yearSpinner, "#");
        yearSpinner.setEditor(editor);

        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("Export PDF");
        
        filterPanel.add(new JLabel("Bulan:"));
        filterPanel.add(monthCombo);
        filterPanel.add(new JLabel("Tahun:"));
        filterPanel.add(yearSpinner);
        filterPanel.add(refreshBtn);
        filterPanel.add(exportBtn);
        
        // Content Panel dengan Tab
        JTabbedPane monthlyTabs = new JTabbedPane();
        monthlyTabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Tab Obat
        JPanel medicinePanel = createMonthlyMedicinePanel();
        // Tab Keuangan
        JPanel financePanel = createMonthlyFinancePanel();
        
        monthlyTabs.addTab("Obat-obatan", medicinePanel);
        monthlyTabs.addTab("Keuangan", financePanel);
        
        refreshBtn.addActionListener(e -> {
            int month = monthCombo.getSelectedIndex() + 1;
            int year = (Integer) yearSpinner.getValue();
            loadMonthlyData(month, year, medicinePanel, financePanel);
        });
        
        exportBtn.addActionListener(e -> {
            int month = monthCombo.getSelectedIndex() + 1;
            int year = (Integer) yearSpinner.getValue();
            exportMonthlyReport(month, year);
        });
        
        // Initial load
        loadMonthlyData(new Date().getMonth() + 1, 2025, medicinePanel, financePanel);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(monthlyTabs, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMonthlyMedicinePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        
        // Summary
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        summaryPanel.add(createSummaryCard("Total Biaya Obat", "Rp 0", new Color(0x4CAF50)));
        summaryPanel.add(createSummaryCard("Rata-rata per Pasien", "Rp 0", new Color(0x2196F3)));
        summaryPanel.add(createSummaryCard("Total Transaksi", "0", new Color(0xFF9800)));
        
        // Table
        String[] columns = {"Status Darurat", "Jumlah Pasien", "Total Biaya Obat", "Rata-rata Biaya"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Store references for refresh
        panel.putClientProperty("summaryPanel", summaryPanel);
        panel.putClientProperty("tableModel", model);
        
        return panel;
    }
    
    private JPanel createMonthlyFinancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        
        // Summary
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        summaryPanel.add(createSummaryCard("Total Pendapatan", "Rp 0", new Color(0x4CAF50)));
        summaryPanel.add(createSummaryCard("Biaya Pemeriksaan", "Rp 0", new Color(0x2196F3)));
        summaryPanel.add(createSummaryCard("Biaya Obat", "Rp 0", new Color(0xFF9800)));
        summaryPanel.add(createSummaryCard("Jumlah Pasien", "0", new Color(0x9C27B0)));
        
        // Table
        String[] columns = {"Minggu", "Jumlah Pasien", "Total Pendapatan", "Biaya Pemeriksaan", "Biaya Obat"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Store references for refresh
        panel.putClientProperty("summaryPanel", summaryPanel);
        panel.putClientProperty("tableModel", model);
        
        return panel;
    }
    
    private void loadMonthlyData(int month, int year, JPanel medicinePanel, JPanel financePanel) {
        loadMonthlyMedicineData(month, year, medicinePanel);
        loadMonthlyFinanceData(month, year, financePanel);
    }
    
    private void loadMonthlyMedicineData(int month, int year, JPanel panel) {
        JPanel summaryPanel = (JPanel) panel.getClientProperty("summaryPanel");
        DefaultTableModel model = (DefaultTableModel) panel.getClientProperty("tableModel");
        model.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT emergency_status, COUNT(*) as patient_count, " +
                        "SUM(medicine_fee) as total_medicine, AVG(medicine_fee) as avg_medicine " +
                        "FROM patients WHERE MONTH(created_at) = ? AND YEAR(created_at) = ? " +
                        "GROUP BY emergency_status ORDER BY emergency_status DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();
            
            double grandTotal = 0;
            int totalTransactions = 0;
            
            while (rs.next()) {
                boolean emergencyStatus = rs.getBoolean("emergency_status");
                int count = rs.getInt("patient_count");
                double total = rs.getDouble("total_medicine");
                double avg = rs.getDouble("avg_medicine");
                
                String statusText = emergencyStatus ? "Darurat" : "Normal";
                
                model.addRow(new Object[]{
                    statusText, count,
                    currencyFormat.format(total),
                    currencyFormat.format(avg)
                });
                
                grandTotal += total;
                totalTransactions += count;
            }
            
            double avgPerPatient = totalTransactions > 0 ? grandTotal / totalTransactions : 0;
            updateSummaryCard(summaryPanel, 0, currencyFormat.format(grandTotal));
            updateSummaryCard(summaryPanel, 1, currencyFormat.format(avgPerPatient));
            updateSummaryCard(summaryPanel, 2, String.valueOf(totalTransactions));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading monthly medicine data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadMonthlyFinanceData(int month, int year, JPanel panel) {
        JPanel summaryPanel = (JPanel) panel.getClientProperty("summaryPanel");
        DefaultTableModel model = (DefaultTableModel) panel.getClientProperty("tableModel");
        model.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT " +
                        "WEEK(created_at, 1) - WEEK(DATE_SUB(created_at, INTERVAL DAYOFMONTH(created_at) - 1 DAY), 1) + 1 as week_num, " +
                        "COUNT(*) as patient_count, " +
                        "SUM(examination_fee + medicine_fee) as total_revenue, " +
                        "SUM(examination_fee) as total_exam, " +
                        "SUM(medicine_fee) as total_medicine " +
                        "FROM patients " +
                        "WHERE MONTH(created_at) = ? AND YEAR(created_at) = ? " +
                        "GROUP BY week_num ORDER BY week_num";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();
            
            double grandTotal = 0;
            double totalExam = 0;
            double totalMedicine = 0;
            int totalPatients = 0;
            
            while (rs.next()) {
                int weekNum = rs.getInt("week_num");
                int patientCount = rs.getInt("patient_count");
                double revenue = rs.getDouble("total_revenue");
                double exam = rs.getDouble("total_exam");
                double medicine = rs.getDouble("total_medicine");
                
                model.addRow(new Object[]{
                    "Minggu " + weekNum,
                    integerFormat.format(patientCount), // Gunakan integerFormat
                    currencyFormat.format(revenue),
                    currencyFormat.format(exam),
                    currencyFormat.format(medicine)
                });
                
                grandTotal += revenue;
                totalExam += exam;
                totalMedicine += medicine;
                totalPatients += patientCount;
            }
            
            updateSummaryCard(summaryPanel, 0, currencyFormat.format(grandTotal));
            updateSummaryCard(summaryPanel, 1, currencyFormat.format(totalExam));
            updateSummaryCard(summaryPanel, 2, currencyFormat.format(totalMedicine));
            updateSummaryCard(summaryPanel, 3, integerFormat.format(totalPatients)); // Gunakan integerFormat
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading monthly finance data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ==================== HELPER METHODS ====================
    private int calculateAge(Date birthDate) {
        if (birthDate == null) return 0;
        
        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar birth = java.util.Calendar.getInstance();
        birth.setTime(birthDate);
        
        int age = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR);
        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
    
    private JPanel createSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), 4, 10, 10);
                g2d.setColor(new Color(220, 220, 220));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setPreferredSize(new Dimension(200, 80)); // Tetapkan ukuran kartu

        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        valueLabel.setName("valueLabel"); // For easy reference
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void updateSummaryCard(JPanel summaryPanel, int cardIndex, String newValue) {
        if (summaryPanel != null && cardIndex < summaryPanel.getComponentCount()) {
            JPanel card = (JPanel) summaryPanel.getComponent(cardIndex);
            for (Component comp : card.getComponents()) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if ("valueLabel".equals(label.getName())) {
                        label.setText(newValue);
                        break;
                    }
                }
            }
        }
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0x96A78D));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(0x96A78D, true));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        
                // Atur lebar kolom tabel
        if (table.getColumnCount() >= 6) {
            table.getColumnModel().getColumn(0).setPreferredWidth(150); // Tanggal
            table.getColumnModel().getColumn(1).setPreferredWidth(120); // Kode Pasien
            table.getColumnModel().getColumn(2).setPreferredWidth(200); // Nama Pasien
            table.getColumnModel().getColumn(3).setPreferredWidth(150); // Biaya Pemeriksaan
            table.getColumnModel().getColumn(4).setPreferredWidth(150); // Biaya Obat
            table.getColumnModel().getColumn(5).setPreferredWidth(150); // Total
        }
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
    
    private String getDateCondition(String period) {
        switch (period) {
            case "Hari Ini":
                return "DATE(created_at) = CURDATE()";
            case "Minggu Ini":
                return "YEARWEEK(created_at, 1) = YEARWEEK(CURDATE(), 1)";
            case "Bulan Ini":
                return "MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())";
            case "Tahun Ini":
                return "YEAR(created_at) = YEAR(CURDATE())";
            default:
                return "DATE(created_at) = CURDATE()";
        }
    }
    
    // ==================== EXPORT METHODS ====================
    private void exportFinancialReport() {
        JOptionPane.showMessageDialog(this,
            "Fitur export PDF akan diimplementasikan menggunakan library iText atau Apache PDFBox.\n" +
            "Silakan tambahkan library tersebut ke project Anda.",
            "Export PDF", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportDailyPatientReport(Date date) {
        JOptionPane.showMessageDialog(this,
            "Export laporan harian pasien untuk tanggal " + new SimpleDateFormat("dd/MM/yyyy").format(date),
            "Export PDF", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportInpatientReport() {
        JOptionPane.showMessageDialog(this,
            "Export laporan rawat inap",
            "Export PDF", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportMonthlyReport(int month, int year) {
        String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                          "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        JOptionPane.showMessageDialog(this,
            "Export laporan bulanan untuk " + months[month - 1] + " " + year,
            "Export PDF", JOptionPane.INFORMATION_MESSAGE);
    }
}