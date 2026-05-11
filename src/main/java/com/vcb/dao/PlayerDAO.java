package com.vcb.dao;

import com.vcb.config.DatabaseConfig;
import com.vcb.model.Player;
import java.sql.*;

/**
 * 玩家 DAO — 註冊、登入、更新勝敗
 * 密碼以 hashCode() 儲存（示範用，實際專案應使用 BCrypt 等安全雜湊）
 */
public class PlayerDAO {

    /** 註冊新玩家（回傳新建的 ID，帳號重複或失敗回傳 -1） */
    public int register(String username, String passwordHash) {
        String sql = "INSERT INTO players (username, password_hash) VALUES (?, ?) RETURNING id";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                System.out.println("❌ 帳號已存在！");
            } else {
                System.err.println("註冊失敗: " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * 登入驗證（帳號 + 密碼雜湊比對）
     * 找到才回傳 Player 物件，否則回傳 null
     */
    public Player login(String username, String passwordHash) {
        String sql = "SELECT * FROM players WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Player(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("wins"),
                    rs.getInt("losses")
                );
            }
        } catch (SQLException e) {
            System.err.println("登入失敗: " + e.getMessage());
        }
        return null;
    }

    /** 勝場 +1 */
    public void addWin(int playerId) {
        updateStat(playerId, "wins");
    }

    /** 敗場 +1 */
    public void addLoss(int playerId) {
        updateStat(playerId, "losses");
    }

    /**
     * 更新玩家統計欄位
     * column 只會傳入 "wins" 或 "losses"（內部呼叫，無 SQL Injection 風險）
     */
    private void updateStat(int playerId, String column) {
        String sql = "UPDATE players SET " + column + " = " + column + " + 1 WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新戰績失敗: " + e.getMessage());
        }
    }
}
