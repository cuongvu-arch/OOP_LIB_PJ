package models.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String PROD_DB_URL = "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/lib_db";
    private static final String PROD_USER = "25xfcWkBUV5jE87.root";
    private static final String PROD_PASSWORD = "ptN1YdMHICOAK6AH";
    private static final String TEST_DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL";

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