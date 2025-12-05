-- Schema untuk sistem manajemen rawat inap rumah sakit jiwa

-- Tabel untuk booking kamar rawat inap
CREATE TABLE IF NOT EXISTS room_bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(50) NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    check_in_date TIMESTAMP NOT NULL,
    check_out_date TIMESTAMP,
    status ENUM('Booking', 'Check-in', 'Check-out', 'Cancelled') DEFAULT 'Booking',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_cost DECIMAL(10,2) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabel untuk jadwal konsultasi
CREATE TABLE IF NOT EXISTS consultation_schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(50) NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    doctor_code VARCHAR(50) NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    consultation_date DATE NOT NULL,
    start_time VARCHAR(10) NOT NULL,
    end_time VARCHAR(10) NOT NULL,
    status ENUM('Scheduled', 'Completed', 'Cancelled', 'No-show') DEFAULT 'Scheduled',
    room VARCHAR(100),
    notes TEXT,
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabel untuk jadwal rawat inap
CREATE TABLE IF NOT EXISTS inpatient_schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(50) NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    doctor_code VARCHAR(50) NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    admission_date TIMESTAMP NOT NULL,
    discharge_date TIMESTAMP,
    status ENUM('Scheduled', 'Admitted', 'Discharged', 'Cancelled') DEFAULT 'Scheduled',
    diagnosis TEXT,
    notes TEXT,
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabel untuk jam shift (untuk referensi)
CREATE TABLE IF NOT EXISTS shift_times (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shift_name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    description TEXT
);

-- Tambahkan data shift default
INSERT INTO shift_times (shift_name, start_time, end_time, description) VALUES
('Pagi (06:00 - 14:00)', '06:00:00', '14:00:00', 'Shift Pagi'),
('Siang (14:00 - 22:00)', '14:00:00', '22:00:00', 'Shift Siang'),
('Malam (22:00 - 06:00)', '22:00:00', '06:00:00', 'Shift Malam')
ON DUPLICATE KEY UPDATE shift_name=shift_name; -- Hanya menambahkan jika belum ada

-- Index untuk performa pencarian
CREATE INDEX idx_room_bookings_patient ON room_bookings(patient_code);
CREATE INDEX idx_room_bookings_room ON room_bookings(room_number);
CREATE INDEX idx_room_bookings_date ON room_bookings(check_in_date);

CREATE INDEX idx_consultation_patient ON consultation_schedules(patient_code);
CREATE INDEX idx_consultation_doctor ON consultation_schedules(doctor_code);
CREATE INDEX idx_consultation_date ON consultation_schedules(consultation_date);

CREATE INDEX idx_inpatient_patient ON inpatient_schedules(patient_code);
CREATE INDEX idx_inpatient_doctor ON inpatient_schedules(doctor_code);
CREATE INDEX idx_inpatient_room ON inpatient_schedules(room_number);
CREATE INDEX idx_inpatient_admission ON inpatient_schedules(admission_date);