import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Scanner;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.inisialisasiTabel();
        Bank bankYoBank = Bank.getInstance();
        bankYoBank.loadFromFile();

        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n--- BANK DIGITAL UT ---");
            System.out.println("1. Daftar Nasabah");
            System.out.println("2. Lihat Semua Nasabah");
            System.out.println("3. Login (Transfer/Cek Saldo)");
            System.out.println("4. Keluar");
            System.out.print("Pilih menu: ");
            int pilihan = sc.nextInt();
            sc.nextLine(); // Buat "buang" hantu ENTER!

            try {
                switch (pilihan) {
                    case 1:
                        // LOGIKA DAFTAR
                        System.out.println("\n--- PENDAFTARAN NASABAH BARU ---");
                        System.out.println("Masukan NAMA LENGKAP");
                        String namaBaru = sc.nextLine();
                        System.out.print("Masukkan PIN (6 digit): ");
                        String pinBaru = sc.next();
                        Nasabah.validasiFormat(pinBaru);
                            try {
                                System.out.print("Setoran Awal: Rp");
                                BigDecimal saldoBaru = new BigDecimal(sc.next());
                                sc.nextLine(); // SAPU HANTU ENTER

                        // 1. BUAT OBJEK DAN MEMANGGIL CONSTUCTOR NASABAH
                                Nasabah nasabahBaru = new Nasabah(namaBaru, pinBaru, saldoBaru);
                        
                        // 2. MASUKIN KE GUDANG
                                bankYoBank.tambahNasabah(nasabahBaru);
                                bankYoBank.simpanKeFile();
                        
                                System.out.println("Berhasil! No Rekening Anda: " + nasabahBaru.getNoRekening());
                            } catch (Exception e) {
                                System.out.println("ERROR: " + e.getMessage());
                            }
                        
                        break;
                    case 2:
                        // DAFTAR NASABAH
                        System.out.println("\n--- DAFTAR NASABAH ---");
                        Collection<Nasabah> daftar = bankYoBank.getSemuaNasabah();
                        if (daftar.isEmpty()) {
                            System.out.println("Bank Kosong");
                        }else{
                            for (Nasabah akun : daftar) {
                                System.out.println(akun);
                            }
                        }
                        break;
                    case 3:
                        // LOGIKA LOGIN & TRANSAKSI
                        // INPUT NO REK
                            System.out.println("Masukan No Rekening");
                            String inputRekening = sc.next();
                        // bisa buat method baru lgi nanti tinggal panggil akunAktif = findNasasbah(daftarNasabah, inputRekening)
                            Nasabah akunAktif = bankYoBank.cariNasabah(inputRekening);

                            if (akunAktif == null) {
                                System.out.println("Rekening tidak terdaftar");
                                break;
                            }
                            //---- CEK PIN -----
                            boolean loginBerhasil = false;
                            while (!loginBerhasil && !akunAktif.getStatusBlokir()) {
                                System.out.println("Masukan PIN Anda :");
                                String inputPin = sc.next();
                                if (inputPin.equals("0")) break;
                                
                                try {
                                    if (akunAktif.cekLogin(inputPin)) {
                                    System.out.println("Login Berhasil!");
                                    loginBerhasil = true;
                                }
                                } catch (Exception e) {
                                    System.out.println("ERROR: " + e.getMessage());
                                    bankYoBank.simpanKeFile();
                                }
                                
                                if (loginBerhasil) {
                                    // MASUK SUB-MENU
                                    //---- SUB MENU ----
                                boolean logout = false;
                                while (!logout) {
                                System.out.println("\n --- MENU TRANSAKSI ---" + akunAktif.getNoRekening());
                                System.out.println("1. Cek Saldo");
                                System.out.println("2. Transfer");
                                System.out.println("3. Cek Mutasi");
                                System.out.println("4. LogOut");
                                System.out.println("Pilih :");
                                int subMenu = sc.nextInt();

                                switch (subMenu) {
                                    case 1:
                                        System.out.println("Saldo anda Rp:" + akunAktif.getSaldo().setScale(2, RoundingMode.HALF_UP));
                                        break;

                                    case 2:
                                        System.out.println("Masukan No Rekening Tujuan :");
                                        String rekTujuan = sc.next();
                                        Nasabah penerima = bankYoBank.cariNasabah(rekTujuan);

                                            if (penerima == null) {
                                                System.out.println("ERROR Rekening tujuan tidak terdaftar");
                                                // Fungsi Break dipertanyakan
                                                break;
                                            }else{
                                                System.out.println("Jumlah Transfer : Rp");
                                            BigDecimal jumlah = new BigDecimal(sc.next());

                                            try {
                                                akunAktif.transfer(penerima, jumlah);
                                                //Catat Transaksi
                                                bankYoBank.catatTransaksi(akunAktif.getNoRekening(), rekTujuan, jumlah);

                                                bankYoBank.simpanKeFile();
                                                System.out.println("Transfer Berhasil ke " + penerima.getNoRekening());
                                            } catch (Exception e) {
                                                // gw ga yakin fungsi ini
                                                System.out.println("ERROR : " + (e.getMessage() != null ? e.getMessage() : e.toString()));
                                            }
                                            
                                            }
                                        break;
                                    case 3: // MUTASI TRANSAKSI
                                        System.out.println("\n--- RIWAYAT TRANSAKSI (" + akunAktif.getNoRekening() + ")---");
                                        java.util.List<String> history = bankYoBank.getMutasiList(akunAktif.getNoRekening());

                                            if (history.isEmpty()) {
                                                System.out.println("Belum ada riwayat.");
                                            }else{
                                                for (String barisMutasi : history) {
                                                    System.out.println(barisMutasi);
                                                }
                                            }
                                        break;
                                    case 4:
                                        logout = true;
                                        loginBerhasil = true;
                                        System.out.println("Logout Berhasil");
                                            break;
                                    default:
                                        System.out.println("Pilihan Salah");
                                        break;
                                        }
                                    }
                                }
                            }
                        break;
                    case 4:
                        running = false;
                        System.out.println("Terima kasih!");
                        break;
                    case 5:
                        System.out.println("Login Admin, 3 urutan saldo nasabah terbesar");
                            List<Nasabah> top3 = bankYoBank.getTopNasabah(3);
                            for (Nasabah nasabah : top3) {
                                System.out.println(nasabah);
                            }
                        break;
                    default:
                        System.out.println("Pilihan tidak valid!");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
        sc.close();
    }
}