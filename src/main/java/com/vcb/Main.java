package com.vcb;

import com.vcb.config.DatabaseConfig;
import com.vcb.view.MenuView;
import java.util.Scanner;

/**
 * VTuber Card Battle — 程式入口
 *
 * 啟動前請確保：
 *   1. PostgreSQL 正在運行
 *   2. 已建立資料庫 vcb 並執行 cards_seed.sql
 *   3. DatabaseConfig.java 中的帳號密碼正確
 *   4. postgresql-42.7.3.jar 在 lib/ 資料夾中
 *
 * 編譯：
 *   javac -cp "lib/*" -d out $(find src -name "*.java")
 *
 * 執行：
 *   java -cp "out:lib/*" com.vcb.Main          (Mac/Linux)
 *   java -cp "out;lib/*" com.vcb.Main          (Windows)
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // 先測試資料庫連線，失敗時提早終止
            if (DatabaseConfig.testConnection()) {
                System.out.println("✅ 資料庫連線成功！\n");
            } else {
                System.err.println("❌ 資料庫連線失敗！請確認 PostgreSQL 是否啟動。");
                return;
            }

            MenuView menu = new MenuView(scanner);
            menu.start();

        } catch (Exception e) {
            System.err.println("❌ 發生錯誤: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();  // 確保 Scanner 釋放 System.in
        }
    }
}
