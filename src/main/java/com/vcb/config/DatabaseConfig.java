package com.vcb.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 資料庫連線設定（純 JDBC，不需要 Maven）
 *
 * 啟動前請確認：
 *   1. PostgreSQL 已啟動
 *   2. 已建立資料庫 vcb 並執行 cards_seed.sql
 *   3. 下方帳號密碼與你的 PostgreSQL 設定一致
 *
 * 預設連線資訊：
 *   帳號：postgres  密碼：my_secret_password
 */
public class DatabaseConfig {

    // === 請依照你的環境修改以下三個設定 ===
    private static final String URL      = "jdbc:postgresql://localhost:5432/vcb";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "my_secret_password";

    static {
        // 程式啟動時載入 JDBC 驅動程式（只需執行一次）
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ 找不到 PostgreSQL JDBC Driver！");
            System.err.println("   請確認 postgresql-42.7.3.jar 在 lib/ 資料夾中，且已加入 classpath。");
            throw new RuntimeException(e);
        }
    }

    /**
     * 取得資料庫連線
     * 每次呼叫都建立新連線，使用完畢務必 close()
     * 建議搭配 try-with-resources：try (Connection conn = getConnection()) { ... }
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** 測試連線是否正常（供 Main.java 啟動時檢查用） */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
