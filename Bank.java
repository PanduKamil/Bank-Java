import java.util.HashMap;
import java.util.Collection;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class Bank {
    private HashMap<String, Nasabah> mapNasabah;
    private static Bank instance;

    private NasabahDAO nasabahDAO = new NasabahDAO();

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
        nasabahDAO.tambahNasabah(akun);        

        mapNasabah.put(akun.getNoRekening(), akun);

    }
    public void loadFromDatabase() {
        List<Nasabah> fromDB = nasabahDAO.loadAll();
        for (Nasabah n : fromDB) {
        mapNasabah.put(n.getNoRekening(), n);

        int angkaRek = Integer.parseInt(n.getNoRekening().substring(4));
        if(angkaRek >= Nasabah.getCounter()) Nasabah.setCounter(angkaRek + 1);

        }
    }
    public Nasabah cariNasabah(String noRek) {
        return mapNasabah.get(noRek);
    }
    public Collection<Nasabah> getSemuaNasabah(){
        return mapNasabah.values();
    }
    public List<Transaksi> getMutasiList(String noRek){
        return nasabahDAO.getMutasiList(noRek);

    }
    public void prosesTransfer(String asal, String tujuan, BigDecimal jumlah){
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            conn.setAutoCommit(false);

            Nasabah pengirim = cariNasabah(asal);
            Nasabah penerima = cariNasabah(tujuan);

            pengirim.transfer(penerima, jumlah);

            nasabahDAO.updateSaldoDatabase(pengirim, conn);
            nasabahDAO.updateSaldoDatabase(penerima, conn);

            nasabahDAO.catatTransaksi(asal, tujuan, jumlah, "TRANSFER", conn);

            conn.commit();
            System.out.println("Transfer Berhasil & Data Saved");
            
        } catch (Exception e) {
        try {
            if (conn != null) {
                conn.rollback(); // [KUNCI ROLLBACK 3] Batalin semua jika ada satu aja yang gagal
                System.err.println("!! TRANSACTION ROLLBACK: Saldo Aman !!");
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        throw new RuntimeException(e.getMessage());
        } finally {
        // Jangan lupa tutup koneksi manual karena autoCommit tadi kita matiin
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
        
    public void updateDataNasabah(Nasabah n){
        try (Connection conn = DatabaseConnection.getConnection()){
            nasabahDAO.updateSaldoDatabase(n, conn);
        } catch (Exception e) {
            System.err.println("Gagal sinkron data: " + e.getMessage());
        }
        
    }

    public BigDecimal getSaldoSekarang(String noRekening){
        System.out.println("Debug: Mencoba cek saldo untuk" + noRekening);
        return nasabahDAO.getSaldoTerbaru(noRekening);
    }
    public void prosesTariktunai(String noRek, BigDecimal jumlah){
        Nasabah n = cariNasabah(noRek);
        
        n.kurangiSaldo(jumlah);
        
        nasabahDAO.updateSaldoDatabase(noRek, "ATM", jumlah, "TARIK TUNAI");
    }
    public void prosesSetorTunai(String noRek, BigDecimal jumlah){
        Nasabah n = cariNasabah(noRek);
        
        n.tambahSaldo(jumlah);
        
        nasabahDAO.updateSaldoDatabase("CASH", noRek, jumlah, "SETOR TUNAI");
    }


    
}
