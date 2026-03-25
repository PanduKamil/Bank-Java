import java.math.BigDecimal;

public class Nasabah{
        private String pin;
        private int percobaan;
        private boolean isBlocked;
        private BigDecimal saldo;
        private String noRekening;
        private static int counter = 1001; 
        private String nama;


    
        public Nasabah(String nama, String pinAwal, BigDecimal saldoAwal) {
        validasiFormatNama(nama);
        validasiFormat(pinAwal);
            //this.pin = SecurityUtil.hashPIN(pinAwal); // Simpen hasil acakan, bukan angka asli
            this.nama = nama;
            this.pin = SecurityUtil.hashPIN(pinAwal);
            this.percobaan = 0;
            this.isBlocked = false;
            this.saldo = saldoAwal;
            this.noRekening = "REK-" + counter;
            counter++;
    }
    public Nasabah(String noRek, String nama, String pin, BigDecimal saldo, boolean isBlocked, int percobaan){
        validasiFormatNama(nama);
        //this.pin = SecurityUtil.hashPIN(pinAwal); // Simpen hasil acakan, bukan angka asli
        this.noRekening = noRek;
        this.nama = nama;
        this.pin = pin;
        this.saldo = saldo;
        this.isBlocked = isBlocked;
        this.percobaan = percobaan;
    }
    public static void validasiFormat(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input Tidak Boleh Kosong");
        }
        if (input.length() != 6) {
            throw new IllegalArgumentException("Format pin salah! Harus 6 digit.");
        }
            try {
                Integer.parseInt(input);
            } catch (Exception e) {
                throw new IllegalArgumentException("Format pin salah! Harus Angka.");
            }
    }
    public boolean cekLogin(String input){
        if (isBlocked) {
            throw new RuntimeException(" Akun anda terblokir!! Silahkan hubungin Admin");
        }
        try {
            validasiFormat(input);
        } catch (IllegalArgumentException e) {
            this.percobaan++;
            if (this.percobaan >= 3) this.isBlocked = true;
            throw e;
        }
        // PIN input dari user diacak dulu, baru dibandingin sama yang di DB
        //if (SecurityUtil.hashPIN(input).equals(this.pin)) {
        //    percobaan = 0;
        //   return true;
        //}
        if (SecurityUtil.hashPIN(input).equals(this.pin)) {
            percobaan = 0;
            return true;
        }else{
            this.percobaan++;
             if (percobaan >= 3) {
            this.isBlocked = true;
                throw new RuntimeException("PIN SALAH 3X!! akun anda  otomatis terblokir");
            }
        }
        throw new RuntimeException(" PIN salah!!! " + "sisa percobaan " + (3 - this.percobaan) + "percobaan");
    }
    public void gantiPin(String pinBaru) {
        validasiFormat(pinBaru);
        this.pin = pinBaru;
    }
    public void resetPercobaan(){
        this.percobaan = 0;
    }
    public boolean getStatusBlokir() {
            return isBlocked;
    }
    public int getPercobaan(){
        return this.percobaan;
    }
    public void tambahSaldo(BigDecimal jumlah){
        if(jumlah.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("ERROR tidak bisa minus (-) ");
        }
        this.saldo = saldo.add(jumlah);
    }
    public void kurangiSaldo(BigDecimal jumlah){
        
        if(saldo.compareTo(jumlah) < 0){
            throw new IllegalArgumentException("Saldo tidak mencukupi");
        }
        this.saldo = saldo.subtract(jumlah);
    }  
    public void transfer(Nasabah target, BigDecimal jumlah){
        if (this.getStatusBlokir()) {
        throw new IllegalArgumentException("AKUN ANDA TERBLOKIR, TIDAK BISA TRANSAKSI");
    }
        if (target.getStatusBlokir()) {
            throw new IllegalArgumentException("AKUN TUJUAN TERBLOKIR");
        }
        if (this == target) {
            throw new IllegalArgumentException("TIDAK BISA MENGIRIM KE AKUN YANG SAMA ");
        }
        this.kurangiSaldo(jumlah);
        target.tambahSaldo(jumlah);
    }
    public BigDecimal getSaldo(){
        return this.saldo;
    }
    public String getNoRekening(){
        return this.noRekening;
    }
    public String getNama(){
        return nama;
    }
    public static boolean isNamaValid(String nama){
        return nama.matches("^[a-zA-Z\\s]+$");
    }
    public static boolean isAngkaSemua(String input){
        return input.matches("^[0-9]+$");
    }
    public void validasiFormatNama(String input){
        if (!isNamaValid(input)) {
            throw new IllegalArgumentException("Nama mengandung karakter terlarang!!");
        }
    }
    public void resetBlokir(){
        this.isBlocked = false;
        this.percobaan = 0;
    }
    public static void setCounter(int baru){
        counter = baru;
    }
    public static int getCounter(){
        return counter;
    }
    public String getPinSimpan(){
        return this.pin;
    }
    @Override
    public String toString() {
    return "No Rek: " + this.noRekening + " | Nama : "+ this.nama +" | Saldo: Rp" + this.saldo;
    }
  

}