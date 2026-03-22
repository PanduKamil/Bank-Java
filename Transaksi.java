import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaksi {
    private String asal, tujuan, jenis;
    private BigDecimal jumlah;
    private Timestamp tanggal;

    public Transaksi(String asal,String tujuan,BigDecimal jumlah, String jenis, Timestamp tanggal){
        this.asal = asal;
        this.tujuan = tujuan;
        this.jumlah = jumlah;
        this.jenis = jenis;
        this.tanggal = tanggal;
    }
    public String getJenis(){
        return jenis;
    }

    public String getAsal() {
        return asal;
    }
    public String getTujuan(){
        return tujuan;
    }
    public BigDecimal getJumlah(){
        return jumlah;
    }
    public Timestamp getTanggal(){
        return tanggal;
    }
    public String getStatus(String noRekeningAktif){
        if (this.asal.equals(noRekeningAktif)) {
            return "KELUAR";
        }else{
            return "MASUK";
        }
    }
    public String getDecStatus(String noRekeningAktif){
        String lawan = this.asal.equals(noRekeningAktif) ? this.tujuan : this.asal;
        String preposisi = this.asal.equals(noRekeningAktif) ? "Ke: " : "Dari: ";
        return String.format("%-6s %s", lawan + preposisi);
    }
    public String getNominalFormat(String noRekeningAktif, java.text.Format format){
        String tanda = this.asal.equals(noRekeningAktif) ? "-Rp" : "+Rp";
        return tanda + format.format(jumlah);
    }
}
