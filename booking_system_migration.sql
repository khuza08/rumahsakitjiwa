-- Migrasi database: Tambahkan tabel booking untuk sistem rawat inap
-- Skema ini menyesuaikan struktur yang sudah ada dalam database rumahsakitjiwa

-- Tabel untuk booking kamar rawat inap
CREATE TABLE IF NOT EXISTS room_bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(20) NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE,
    status ENUM('Booking', 'Check-in', 'Check-out', 'Cancelled') DEFAULT 'Booking',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_cost DECIMAL(10,2) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_code (patient_code),
    INDEX idx_room_number (room_number),
    INDEX idx_check_in_date (check_in_date),
    INDEX idx_status (status)
);

-- Tabel untuk jadwal konsultasi
CREATE TABLE IF NOT EXISTS consultation_schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(20) NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    doctor_code VARCHAR(20) NOT NULL,
    doctor_name VARCHAR(100) NOT NULL,
    consultation_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('Scheduled', 'Completed', 'Cancelled', 'No-show') DEFAULT 'Scheduled',
    room VARCHAR(100),
    notes TEXT,
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_code (patient_code),
    INDEX idx_doctor_code (doctor_code),
    INDEX idx_consultation_date (consultation_date),
    INDEX idx_status (status)
);

-- Tabel untuk jadwal rawat inap
CREATE TABLE IF NOT EXISTS inpatient_schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(20) NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    doctor_code VARCHAR(20) NOT NULL,
    doctor_name VARCHAR(100) NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    admission_date TIMESTAMP NOT NULL,
    discharge_date TIMESTAMP,
    status ENUM('Scheduled', 'Admitted', 'Discharged', 'Cancelled') DEFAULT 'Scheduled',
    diagnosis TEXT,
    notes TEXT,
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_code (patient_code),
    INDEX idx_doctor_code (doctor_code),
    INDEX idx_room_number (room_number),
    INDEX idx_admission_date (admission_date),
    INDEX idx_status (status)
);

-- Tabel untuk jam shift (untuk referensi sesuai dengan format shift di tabel schedules)
CREATE TABLE IF NOT EXISTS shift_times (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shift_name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tambahkan data shift default sesuai dengan format yang digunakan di tabel schedules
INSERT INTO shift_times (shift_name, start_time, end_time, description) VALUES
('Pagi (06:00 - 14:00)', '06:00:00', '14:00:00', 'Shift Pagi'),
('Siang (14:00 - 22:00)', '14:00:00', '22:00:00', 'Shift Siang'),
('Malam (22:00 - 06:00)', '22:00:00', '06:00:00', 'Shift Malam');

-- Update struktur tabel doctors agar sesuai dengan kebutuhan sistem booking
-- Menambahkan kolom yang mungkin dibutuhkan
-- Hanya tambahkan kolom jika belum ada
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'doctors'
AND COLUMN_NAME = 'specialization';

SET @sqlstmt = IF(@col_exists = 0, 'ALTER TABLE doctors ADD COLUMN specialization VARCHAR(100) DEFAULT ''Sp.KJ''', 'SELECT 1');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'doctors'
AND COLUMN_NAME = 'status';

SET @sqlstmt = IF(@col_exists = 0, 'ALTER TABLE doctors ADD COLUMN status ENUM(''Aktif'', ''Tidak Aktif'') DEFAULT ''Aktif''', 'SELECT 1');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update struktur tabel patients agar sesuai dengan kebutuhan sistem booking
-- Menambahkan kolom yang mungkin dibutuhkan
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'patients'
AND COLUMN_NAME = 'status_detailed';

SET @sqlstmt = IF(@col_exists = 0, 'ALTER TABLE patients ADD COLUMN status_detailed ENUM(''Aktif'', ''Rawat Jalan'', ''Rawat Inap'', ''Keluar'') DEFAULT ''Aktif''', 'SELECT 1');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Menyesuaikan field status di tabel patients untuk mencakup lebih banyak status
-- Konversi data yang sudah ada jika kolom baru ditambahkan
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'patients'
AND COLUMN_NAME = 'status_detailed';

SET @sqlstmt = IF(@col_exists = 1, 'UPDATE patients SET status_detailed =
    CASE
        WHEN status = ''Aktif'' THEN ''Aktif''
        WHEN status = ''Rawat Inap'' THEN ''Rawat Inap''
        WHEN status = ''Keluar'' THEN ''Keluar''
        ELSE ''Aktif''
    END', 'SELECT 1');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tambahkan indeks untuk kolom yang sering digunakan dalam pencarian
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'patients'
AND INDEX_NAME = 'idx_status_detailed';

SET @sqlstmt = IF(@index_exists = 0, 'ALTER TABLE patients ADD INDEX idx_status_detailed (status_detailed)', 'SELECT 1');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'doctors'
AND INDEX_NAME = 'idx_status';

SET @sqlstmt = IF(@index_exists = 0, 'ALTER TABLE doctors ADD INDEX idx_status (status)', 'SELECT 1');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tambahkan view untuk laporan booking
CREATE OR REPLACE VIEW v_booking_report AS
SELECT 
    rb.id,
    rb.patient_code,
    rb.patient_name,
    rb.room_number,
    rb.room_type,
    rb.check_in_date,
    rb.check_out_date,
    rb.status,
    rb.total_cost,
    rb.booking_date,
    DATEDIFF(rb.check_out_date, rb.check_in_date) AS duration_days
FROM room_bookings rb;

-- Tambahkan view untuk laporan konsultasi
CREATE OR REPLACE VIEW v_consultation_report AS
SELECT 
    cs.id,
    cs.patient_code,
    cs.patient_name,
    cs.doctor_code,
    cs.doctor_name,
    cs.consultation_date,
    cs.start_time,
    cs.end_time,
    cs.status,
    cs.room,
    cs.scheduled_at
FROM consultation_schedules cs;

-- Tambahkan view untuk laporan rawat inap
CREATE OR REPLACE VIEW v_inpatient_schedule_report AS
SELECT 
    isch.id,
    isch.patient_code,
    isch.patient_name,
    isch.doctor_code,
    isch.doctor_name,
    isch.room_number,
    isch.room_type,
    isch.admission_date,
    isch.discharge_date,
    isch.status,
    isch.diagnosis,
    DATEDIFF(COALESCE(isch.discharge_date, NOW()), isch.admission_date) AS length_of_stay
FROM inpatient_schedules isch;