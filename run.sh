#!/bin/bash

# Konfigurasi folder bin dan library
BIN_DIR="bin"
LIBS="flatlaf-3.4.1.jar:jcalendar-1.4.jar:LGoodDatePicker-11.2.1.jar:mysql-connector-j-9.5.0.jar"
MAIN_CLASS="rumahsakitjiwa.main"

# 1. Cek folder bin, jika belum ada buat dulu
if [ ! -d "$BIN_DIR" ]; then
    echo "Dektori $BIN_DIR tidak ditemukan. Membuat direktori..."
    mkdir -p "$BIN_DIR"
fi

# 2. Kompilasi (Opsional: selalu kompilasi untuk memastikan kode terbaru)
echo "Melakukan kompilasi source code..."
javac -cp ".:$LIBS" -d "$BIN_DIR" src/rumahsakitjiwa/*.java src/rumahsakitjiwa/*/*.java

# Cek apakah kompilasi berhasil
if [ $? -eq 0 ]; then
    echo "Kompilasi berhasil."
    echo "Menjalankan aplikasi..."
    # 3. Jalankan aplikasi
    java -Dsun.java2d.opengl=true -Dsun.java2d.xrender=true -Dsun.java2d.pmoffscreen=false -Dsun.java2d.noddraw=true -cp "$BIN_DIR:$LIBS" --enable-native-access=ALL-UNNAMED "$MAIN_CLASS"
else
    echo "Kompilasi GAGAL. Harap periksa error di atas."
    exit 1
fi
