# Panduan Menjalankan Project Rumah Sakit Jiwa (Medicare)

Gunakan panduan ini jika Anda ingin menjalankan aplikasi tanpa menggunakan NetBeans atau IDE lainnya.

## 0. Cara Cepat (Rekomendasi)
Saya telah menyediakan script executor `run.sh`. Anda cukup menjalankan:
```bash
./run.sh
```
Script ini akan melakukan kompilasi otomatis dan menjalankan aplikasi.

## 1. Persiapan
Pastikan semua file library berikut ada di root directory project:
- `flatlaf-3.4.1.jar`
- `jcalendar-1.4.jar`
- `LGoodDatePicker-11.2.1.jar`
- `mysql-connector-j-9.5.0.jar`

## 2. Kompilasi
Buka terminal di root directory project, lalu jalankan perintah berikut:

```bash
# Membuat folder untuk file hasil kompilasi
mkdir -p bin

# Melakukan kompilasi semua source code
javac -cp ".:*.jar" -d bin src/rumahsakitjiwa/*.java src/rumahsakitjiwa/*/*.java
```

## 3. Menjalankan Aplikasi
Setelah kompilasi berhasil, jalankan perintah berikut untuk membuka aplikasi:

```bash
java -cp "bin:*.jar" --enable-native-access=ALL-UNNAMED rumahsakitjiwa.main
```

> [!NOTE]
> Argumen `--enable-native-access=ALL-UNNAMED` digunakan untuk mendukung fitur-fitur FlatLaf pada versi Java yang lebih baru.

## 4. Troubleshooting Database
Jika aplikasi gagal terhubung ke database, pastikan:
1. MySQL Server sudah berjalan.
2. Database bernama `rumahsakitjiwa` sudah dibuat.
3. Konfigurasi di `src/rumahsakitjiwa/database/DatabaseConnection.java` sesuai dengan username dan password MySQL Anda.
