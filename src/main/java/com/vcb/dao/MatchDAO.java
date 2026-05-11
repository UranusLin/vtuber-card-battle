package com.vcb.dao;

import com.vcb.config.DatabaseConfig;
import java.sql.*;
import java.util.*;

/**
 * 對戰記錄 DAO — 儲存與查詢對戰歷史
 */
public class MatchDAO {

    /** 儲存一筆對戰結果到 match_history 資料表 */
    public void save(int playerId, String opponentName, String playerFaction,
                     String opponentFaction, String result, int turns, int hpLeft) {
        String sql = "INSERT INTO match_history " +
                     "(player_id, opponent_name, player_faction, opponent_faction, result, turns, player_hp_left) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setString(2, opponentName);
            ps.setString(3, playerFaction);
            ps.setString(4, opponentFaction);
            ps.setString(5, result);     // "WIN" or "LOSE"
            ps.setInt(6, turns);
            ps.setInt(7, hpLeft);        // 勝利時為正，失敗時可能為負（疲勞傷害）
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("儲存對戰記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 查詢玩家最近 N 筆對戰記錄（依時間倒序）
     * 回傳 LinkedHashMap 保持欄位的插入順序，方便 View 層依序顯示
     */
    public List<Map<String, Object>> findRecent(int playerId, int limit) {
        List<Map<String, Object>> records = new ArrayList<>();
        String sql = "SELECT * FROM match_history WHERE player_id = ? ORDER BY played_at DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("opponent",   rs.getString("opponent_name"));
                row.put("result",     rs.getString("result"));
                row.put("faction",    rs.getString("player_faction"));
                row.put("oppFaction", rs.getString("opponent_faction"));
                row.put("turns",      rs.getInt("turns"));
                row.put("hpLeft",     rs.getInt("player_hp_left"));
                row.put("date",       rs.getTimestamp("played_at"));
                records.add(row);
            }
        } catch (SQLException e) {
            System.err.println("查詢記錄失敗: " + e.getMessage());
        }
        return records;
    }
}
