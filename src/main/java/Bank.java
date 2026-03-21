import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import main.java.NasabahDAO;

import java.util.List;
import java.sql.*;


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
    public void loadDatabase() {
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
    public List<String> lihatRiwayat(String noRek){
        return nasabahDAO.getMutasiList(noRek);

    }
    public void prosesTransfer(String asal, String tujuan, BigDecimal jumlah){
        Nasabah pengirim = cariNasabah(asal);
        Nasabah penerima = cariNasabah(tujuan);

        pengirim.transfer(penerima, jumlah);

        nasabahDAO.updateSaldoDatabase(pengirim);
        nasabahDAO.updateSaldoDatabase(penerima);

        nasabahDAO.catatTransaksi(asal, tujuan, jumlah, "TRANSFER");
    }


    
}
