import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import java.util.ArrayList;

public class NasabahDAO {

    public void tambahNasabah(Nasabah akun) {
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
  
                System.out.println("[LOG] Nasabah berhasil disimpan ke Database.");
        } catch (Exception e) {
            throw new RuntimeException("Gagal simpan ke Database: " + e.getMessage());
        }
    }

    public List<Nasabah>loadAll() {
        List<Nasabah> list = new  ArrayList<>();

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
                list.add(n);

                System.out.println("[LOG] Data berhasil dimuat dari Database.");
            }
        } catch (SQLException e) {
                System.out.println("[ERROR] Gagal load DB: " + e.getMessage());
        }
        return list;
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
    public java.util.List<Transaksi> getMutasiList(String noRekAktif){
                java.util.List<Transaksi> listHistory = new java.util.ArrayList<>();

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

                        listHistory.add(new Transaksi(asal, tujuan, jumlah, jenis, tanggal));
                    } 
                    
                    }
        } catch (Exception e) {
            throw new RuntimeException("Gagal akses log: " + e.getMessage());
        }
        return listHistory;
    }
    public BigDecimal getSaldoTerbaru(String noRekening){
        String sql = "SELECT saldo FROM nasabah WHERE no_rekening = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, noRekening);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("saldo");
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Gagal ambil saldo: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
