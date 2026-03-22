import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Alamat "Brankas Digital" lo
    private static final String URL = "jdbc:h2:./data/bankDB";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    
    private static Connection connection;

    // Konstruktor private biar gak bisa di-new dari luar
    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Kalau pintunya belum ada atau udah ketutup, kita buka baru
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }


}
