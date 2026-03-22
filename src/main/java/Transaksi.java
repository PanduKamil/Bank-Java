package main.java;

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
}
