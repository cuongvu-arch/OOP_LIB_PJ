package models.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Lớp tiện ích cung cấp phương thức để kết nối đến cơ sở dữ liệu MySQL.
 * <p>
 * Sử dụng JDBC để thiết lập kết nối đến TiDB/MySQL cloud database thông qua URL, username, và password.
 * Mục đích là trung tâm quản lý kết nối trong toàn bộ ứng dụng.
 */
public class DatabaseConnection {
    private static final String PROD_DB_URL = "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/lib_db";
    private static final String PROD_USER = "25xfcWkBUV5jE87.root";
    private static final String PROD_PASSWORD = "ptN1YdMHICOAK6AH";
    private static final String TEST_DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL";

    /**
     * Thiết lập và trả về một kết nối đến cơ sở dữ liệu.
     * <p>
     * Nếu kết nối thất bại, phương thức sẽ in lỗi và trả về null.
     *
     * @return Đối tượng {@link Connection} nếu kết nối thành công, hoặc {@code null} nếu thất bại
     */
    public static Connection getConnection() {
        String env = System.getProperty("env", "prod");
        try {
            if ("test".equals(env)) {
                // H2 in-memory database for testing
                Class.forName("org.h2.Driver");
                return DriverManager.getConnection(TEST_DB_URL, "sa", "");
            } else {
                // MySQL/TiDB for production
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(PROD_DB_URL, PROD_USER, PROD_PASSWORD);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
            return null;
        }
    }
}