import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {
    public static String hashPIN(String pin) {
        try {
            //Memanggil Mesin SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //Proses pengacakan (Input String -> Byte -> Hashing)
            byte[] hash = digest.digest(pin.getBytes());

            //Mengubah byte menjadi Hexadecimal
            StringBuilder hexString = new StringBuilder();
            // Operasi bitwise 0xff buat mastiin angka positif
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)hexString.append('0');
                hexString.append(hex); 
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Ini kalau mesin SHA-256 nggak ketemu di library Java
            throw new RuntimeException("Error: Algoritma Kriptografi tidak ditemukan!", e);
        }
    }   
}

