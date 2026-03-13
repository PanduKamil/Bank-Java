import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.math.BigDecimal;

public class Bank {
    private HashMap<String, Nasabah> mapNasabah;
    public Bank(){
        this.mapNasabah = new HashMap<>();
    }
    public void tambahNasabah(Nasabah akun) {
        if (mapNasabah.containsKey(akun.getNoRekening())) {
            throw new IllegalArgumentException("Nomor Rekening sudah terdaftar");
        }
         mapNasabah.put(akun.getNoRekening(),akun);
         simpanKeFile();
    }
    public Nasabah cariNasabah(String noRek) {
        return mapNasabah.get(noRek);
    }
    public Collection<Nasabah> getSemuaNasabah(){
        return mapNasabah.values();
    }
    public void simpanKeFile(){
        try (PrintWriter writer = new PrintWriter(new FileWriter("nasabah.txt"))){
            for (Nasabah n : mapNasabah.values()) {
                String baris = n.getNoRekening() + "|" + n.getNama() + "|" + n.getSaldo() + "|" + n.getPinSimpan() + "|" + n.getStatusBlokir() + "|" + n.getPercobaan();
                writer.println(baris);
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("Gagal simpan data: " + e.getMessage());
        }
    }

   public void loadFromFile(){
    try (BufferedReader br = new BufferedReader(new FileReader("nasabah.txt"))){   
        String baris;   
        while ((baris = br.readLine()) != null) {
            if (baris.trim().isEmpty()) continue; // Skip baris kosong
            
            String[] hasil = baris.split("\\|");
            if (hasil.length == 6) {
                String noRek = hasil[0];
                String nama = hasil[1];
                BigDecimal saldo = new BigDecimal(hasil[2]);
                String pinAsli = hasil[3];
                // INI YANG GUE MAKSUD: Ubah teks ke boolean
                boolean statusLama = Boolean.parseBoolean(hasil[4]);
                int percobaan = Integer.parseInt(hasil[5]); 

                // MASUKKIN KE CONSTRUCTOR (Pastiin Nasabah.java lo udah punya 5 parameter ini!)
                Nasabah n = new Nasabah(noRek, nama, pinAsli, saldo, statusLama, percobaan);
                
                mapNasabah.put(noRek, n);

                // Update counter biar gak bentrok
                int angkaRek = Integer.parseInt(noRek.substring(4));
                if (angkaRek >= Nasabah.getCounter()) {
                    Nasabah.setCounter(angkaRek + 1);
                }
            }
        }
    } catch (Exception e) {
        System.out.println("INFO: Belum ada data atau file error: " + e.getMessage());
    }
}

    public void catatTransaksi(String rekAsal, String rekTujuan, BigDecimal jumlah){
        try (PrintWriter writer = new PrintWriter(new FileWriter("transaksi.txt", true))) {
            String waktu = java.time.LocalDateTime.now().toString();
            writer.println(rekAsal + "|" + rekTujuan + "|" + jumlah + "|" + waktu);

            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Gagal mencatat mutasi: " + e.getMessage());
        }
    }

    public java.util.List<String> getMutasiList(String noRekAktif){
        java.util.List<String> listHistory = new java.util.ArrayList<>();

        java.io.File fileLog = new java.io.File("transaksi.txt");
        // MENGECEK KETERSEDIAAN FILE
        if(!fileLog.exists()) return listHistory;

        try (BufferedReader br = new BufferedReader(new FileReader(fileLog))) {
            String baris;
            while ((baris = br.readLine()) != null ) {
                String[] kolom = baris.split("\\|");
                if (kolom.length == 4) {
                    String asal = kolom[0];
                    String tujuan = kolom[1];
                    String jumlah = kolom[2];
                    String waktu = (kolom[3].length() >= 19) ? kolom[3].substring(0, 19) : kolom[3];

                    if (asal.equals(noRekAktif)) {
                        listHistory.add("[" + waktu + "] KELUAR -> Ke: " + tujuan + " | -Rp" + jumlah );
                    }else if (tujuan.equals(noRekAktif)) {
                        listHistory.add("[" + waktu + "] MASUK <- Dari: " + tujuan + " | +Rp" + jumlah );
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Gagal akses log: " + e.getMessage());
        }
        return listHistory;
    }





}
