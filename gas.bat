@echo off
:: 1. Kompilasi: ambil semua .java, masukkan hasil .class ke bin
:: Sertakan library H2 di classpath agar tidak error saat compile
javac -d bin -cp ".;lib/h2-2.3.232.jar" *.java

if %errorlevel% == 0 (
    echo Kompilasi Berhasil! Menjalankan...
    :: 2. Menjalankan: Cari class di 'bin' DAN library di 'lib'
    :: Gunakan tanda ; sebagai pemisah di Windows
    java -cp "bin;lib/h2-2.3.232.jar" Main
) else (
    echo Ada yang salah bro, cek error di atas.
)
pause