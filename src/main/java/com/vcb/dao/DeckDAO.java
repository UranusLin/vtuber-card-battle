package com.vcb.dao;

import com.vcb.config.DatabaseConfig;
import com.vcb.model.*;
import java.sql.*;
import java.util.*;

/**
 * 牌組 DAO — 牌組 CRUD 與牌組內容管理
 */
public class DeckDAO {

    /** 取得玩家所有牌組 */
    public List<Deck> findByPlayerId(int playerId) {
        List<Deck> decks = new ArrayList<>();
        String sql = "SELECT * FROM decks WHERE player_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                decks.add(new Deck(rs.getInt("id"), rs.getString("deck_name"), rs.getString("faction")));
            }
        } catch (SQLException e) {
            System.err.println("查詢牌組失敗: " + e.getMessage());
        }
        return decks;
    }

    /** 建立新牌組（回傳新建的 ID，失敗回傳 -1） */
    public int create(int playerId, String deckName, String faction) {
        String sql = "INSERT INTO decks (player_id, deck_name, faction) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // PreparedStatement 參數索引從 1 開始，依 SQL 中 ? 的順序對應
            ps.setInt(1, playerId);
            ps.setString(2, deckName);
            ps.setString(3, faction);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("建立牌組失敗: " + e.getMessage());
        }
        return -1;
    }

    /**
     * 新增卡牌到牌組
     * ON CONFLICT 確保同一張卡重複插入時改為更新數量（upsert）
     */
    public void addCard(int deckId, int cardId, int quantity) {
        String sql = "INSERT INTO deck_cards (deck_id, card_id, quantity) VALUES (?, ?, ?) " +
                     "ON CONFLICT (deck_id, card_id) DO UPDATE SET quantity = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deckId);
            ps.setInt(2, cardId);
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);  // ON CONFLICT 的 SET 也要綁定
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("新增卡牌失敗: " + e.getMessage());
        }
    }

    /** 取得牌組內的所有卡牌 ID 與數量（LinkedHashMap 保持插入順序） */
    public Map<Integer, Integer> getDeckCardIds(int deckId) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT card_id, quantity FROM deck_cards WHERE deck_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deckId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getInt("card_id"), rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            System.err.println("查詢牌組內容失敗: " + e.getMessage());
        }
        return result;
    }

    /** 刪除牌組（關聯的 deck_cards 由 FK CASCADE 自動刪除） */
    public void delete(int deckId) {
        String sql = "DELETE FROM decks WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deckId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("刪除牌組失敗: " + e.getMessage());
        }
    }
}
