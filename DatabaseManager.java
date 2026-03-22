import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseManager {
    public static void inisialisasiTabel() {
        // 1. Siapkan perintah SQL-nya
        String sqlNasabah ="CREATE TABLE IF NOT EXISTS nasabah (" +
                            "no_rekening VARCHAR(20) PRIMARY KEY, " +
                            "nama VARCHAR(100), " +
                            "pin VARCHAR(6), " +
                            "saldo DECIMAL(20,2), " +
                            "is_blocked BOOLEAN DEFAULT FALSE, " +
                            "percobaan INT DEFAULT 0)";

        String sqlTransaksi ="CREATE TABLE IF NOT EXISTS transaksi (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "no_rekening_pengirim VARCHAR(20), " +
                            "no_rekening_penerima VARCHAR(20), " +
                            "jumlah DECIMAL(12,2), " +
                            "jenis_transaksi VARCHAR(20), " + //-- 'TRANSFER', 'TARIK', 'SETOR' 
                            "tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (no_rekening_pengirim) REFERENCES nasabah(no_rekening))";

        // 2. Ambil pintu koneksi dari Singleton kita
        // 3. Suruh tukang (Statement) kirim perintahnya

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlNasabah);
                    stmt.execute(sqlTransaksi);
                    //print untuk develop
                    System.out.println("[LOG] Tabel Nasabah & Transaksi sinkron");
            
        } catch (SQLException e) {
            //print untuk develop
            System.out.println("[ERROR] Gagal inisialisasi: " + e.getMessage());
        }
    }
}
