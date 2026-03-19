import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.List;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Bank {
    private HashMap<String, Nasabah> mapNasabah;
    private static Bank instance;
    DecimalFormat kursIndonesia = new DecimalFormat("###,###.##");

    String pattern = "dd-MM-yyyy HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);

    private Bank(){
        this.mapNasabah = new HashMap<>();
    }
    public static Bank getInstance(){
        if (instance == null) {
            instance = new Bank();
        }
        return instance;
    }
    public List<Nasabah> getTopNasabah(int limit){
        return mapNasabah.values().stream()
        .sorted((a, b) -> b.getSaldo().compareTo(a.getSaldo()))
        .limit(limit)
        .collect(Collectors.toList());
    } 

    public void tambahNasabah(Nasabah akun) {
        if (mapNasabah.containsKey(akun.getNoRekening())) {
            throw new IllegalArgumentException("Nomor Rekening sudah terdaftar");
        }
        String sql = "INSERT INTO nasabah(no_rekening, nama, pin, saldo, is_blocked, percobaan) " +
                     "VALUES(?, ?, ?, ?, ?, ?)";    
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, akun.getNoRekening());
                pstmt.setString(2, akun.getNama());
                pstmt.setString(3, akun.getPinSimpan());
                pstmt.setBigDecimal(4, akun.getSaldo());
                pstmt.setBoolean(5, akun.getStatusBlokir());
                pstmt.setInt(6, akun.getPercobaan());

                pstmt.executeUpdate();
                mapNasabah.put(akun.getNoRekening(), akun);
                System.out.println("[LOG] Nasabah berhasil disimpan ke Database.");
        } catch (Exception e) {
            throw new RuntimeException("Gagal simpan ke Database: " + e.getMessage());
        }
    }
    public void loadFromDatabase() {
        String sql = "SELECT * FROM nasabah";
        try (Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
        ) {
            while (rs.next()) {
                Nasabah n = new Nasabah(
                rs.getString("no_rekening"),
                rs.getString("nama"),
                rs.getString("pin"),
                rs.getBigDecimal("saldo"),
                rs.getBoolean("is_blocked"),
                rs.getInt("percobaan")
                );
                mapNasabah.put(n.getNoRekening(), n);

                int angkaRek = Integer.parseInt(n.getNoRekening().substring(4));
                if(angkaRek >= Nasabah.getCounter()) Nasabah.setCounter(angkaRek + 1);

                System.out.println("[LOG] Data berhasil dimuat dari Database.");
            }
        } catch (SQLException e) {
                System.out.println("[ERROR] Gagal load DB: " + e.getMessage());
        }
    }
    public Nasabah cariNasabah(String noRek) {
        return mapNasabah.get(noRek);
    }
    public Collection<Nasabah> getSemuaNasabah(){
        return mapNasabah.values();
    }
    public void updateSaldoDatabase(Nasabah akun) {
    String sql = "UPDATE nasabah SET saldo = ?, is_blocked = ?, percobaan = ? WHERE no_rekening = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setBigDecimal(1, akun.getSaldo());
        pstmt.setBoolean(2, akun.getStatusBlokir());
        pstmt.setInt(3, akun.getPercobaan());
        pstmt.setString(4, akun.getNoRekening());
        
        pstmt.executeUpdate();
        // System.out.println("[LOG] Database terupdate untuk: " + akun.getNoRekening());
    } catch (SQLException e) {
        System.out.println("[ERROR] Gagal update data ke DB: " + e.getMessage());
    }
    }
    public void catatTransaksi(String rekAsal, String rekTujuan, BigDecimal jumlah, String jenis){
        String sql = "INSERT INTO transaksi(no_rekening_pengirim, no_rekening_penerima, jumlah, jenis_transaksi) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rekAsal);
            pstmt.setString(2, rekTujuan);
            pstmt.setBigDecimal(3, jumlah);
            pstmt.setString(4, jenis);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencatat mutasi: " + e.getMessage());
        }
    }

    public java.util.List<String> getMutasiList(String noRekAktif){
                java.util.List<String> listHistory = new java.util.ArrayList<>();

        String sql = "SELECT * FROM transaksi WHERE no_rekening_pengirim = ? OR no_rekening_penerima = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                    ) {
                        
                    pstmt.setString(1, noRekAktif);
                    pstmt.setString(2, noRekAktif);
                    try (ResultSet rs = pstmt.executeQuery();) {
                        
                    while (rs.next()) {

                        String asal = rs.getString("no_rekening_pengirim");
                        String tujuan = rs.getString("no_rekening_penerima");
                        BigDecimal jumlah = rs.getBigDecimal("jumlah");
                        String jenis = rs.getString("jenis_transaksi");
                        Timestamp tanggal = rs.getTimestamp("tanggal");

                            String saldoFormat = kursIndonesia.format(jumlah);
                            String timeFormat = sdf.format(tanggal);
                            if (asal.equals(noRekAktif)) {
                                listHistory.add("[" + tanggal + "] KELUAR -> Ke: " + tujuan + " | -Rp" + saldoFormat );
                            }else if (tujuan.equals(noRekAktif)) {
                                listHistory.add("[" + tanggal + "] MASUK <- Dari: " + asal + " | +Rp" + saldoFormat );
                            }
                    } 
                    
                    }
        } catch (Exception e) {
            throw new RuntimeException("Gagal akses log: " + e.getMessage());
        }
        return listHistory;
    }
    public void migrasiSekaliJalan() {
    System.out.println("[LOG] Memulai proses migrasi dari nasabah.txt...");
    
    // 1. Baca file .txt (Pakai logika lama lo)
    try (BufferedReader br = new BufferedReader(new FileReader("nasabah.txt"))) {
        String sql = "INSERT INTO nasabah (no_rekening, nama, pin, saldo, is_blocked, percobaan) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String baris;
            int count = 0;
            while ((baris = br.readLine()) != null) {
                if (baris.trim().isEmpty()) continue;
                
                String[] hasil = baris.split("\\|");
                if (hasil.length == 6) {
                    // Masukin ke PreparedStatement
                    pstmt.setString(1, hasil[0]); // noRek
                    pstmt.setString(2, hasil[1]); // nama
                    pstmt.setString(3, hasil[3]); // pin (indeks 3 di txt lo)
                    pstmt.setBigDecimal(4, new BigDecimal(hasil[2])); // saldo
                    pstmt.setBoolean(5, Boolean.parseBoolean(hasil[4])); // statusBlokir
                    pstmt.setInt(6, Integer.parseInt(hasil[5])); // percobaan
                    
                    pstmt.addBatch();
                    count++;
                }
            }
            pstmt.executeBatch();
            System.out.println("[SUCCESS] " + count + " data nasabah berhasil pindah ke Brankas Database!");
            
        }
    } catch (Exception e) {
        System.out.println("[INFO] Migrasi dilewati/Gagal: " + e.getMessage());
    }
}
}
