package com.vcb.dao;

import com.vcb.config.DatabaseConfig;
import com.vcb.model.*;
import com.vcb.model.enums.*;
import java.sql.*;
import java.util.*;

/**
 * 卡牌 DAO — 從資料庫載入卡牌圖鑑
 * 使用 PreparedStatement 防止 SQL Injection
 * try-with-resources 確保連線與 Statement 自動關閉
 */
public class CardDAO {

    /** 載入所有卡牌（依陣營、類型、費用排序） */
    public List<Card> findAll() {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards ORDER BY faction, card_type, mana_cost";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cards.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("載入卡牌失敗: " + e.getMessage());
        }
        return cards;
    }

    /** 依 ID 查找單張卡牌 */
    public Card findById(int id) {
        String sql = "SELECT * FROM cards WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("查詢卡牌失敗: " + e.getMessage());
        }
        return null;
    }

    /** 依陣營篩選卡牌（含中立牌，組牌時使用） */
    public List<Card> findByFaction(String faction) {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards WHERE faction = ? ORDER BY card_type, mana_cost";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, faction);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) cards.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("篩選卡牌失敗: " + e.getMessage());
        }
        return cards;
    }

    /**
     * 將 ResultSet 的一行轉換為對應的 Card 子類別（工廠方法模式）
     * 依 card_type 欄位決定實例化 VTuberCard / TalentCard / SupportCard
     */
    private Card mapRow(ResultSet rs) throws SQLException {
        int id          = rs.getInt("id");
        String name     = rs.getString("name");
        Faction faction = Faction.fromString(rs.getString("faction"));
        String typeStr  = rs.getString("card_type");
        int manaCost    = rs.getInt("mana_cost");
        Rarity rarity   = Rarity.fromInt(rs.getInt("rarity"));
        String effectType  = rs.getString("effect_type");
        int effectValue    = rs.getInt("effect_value");
        String targetType  = rs.getString("target_type");
        String effectDesc  = rs.getString("effect_desc");
        String flavorText  = rs.getString("flavor_text");

        return switch (typeStr) {
            case "VTUBER" -> new VTuberCard(id, name, faction, manaCost, rarity,
                    rs.getInt("attack"), rs.getInt("health"),
                    Ability.fromString(rs.getString("ability")),
                    effectType, effectValue, effectDesc, flavorText);
            case "TALENT" -> new TalentCard(id, name, faction, manaCost, rarity,
                    effectType, effectValue, targetType, effectDesc, flavorText);
            case "SUPPORT" -> new SupportCard(id, name, faction, manaCost, rarity,
                    rs.getInt("attack"), rs.getInt("health"),   // attack/health 欄位在 SUPPORT 卡代表加成值
                    Ability.fromString(rs.getString("ability")),
                    effectDesc, flavorText);
            default -> throw new SQLException("未知卡牌類型: " + typeStr);
        };
    }
}
