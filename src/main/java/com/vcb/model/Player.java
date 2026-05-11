package com.vcb.model;

import com.vcb.model.enums.Faction;
import java.util.*;

/**
 * 玩家狀態 — 管理 HP、魔力、手牌、場上 VTuber、牌組
 */
public class Player {
    private int id;
    private String username;
    private Faction faction;
    private int hp;
    private int maxHp;
    private int mana;       // 當前可用 SC 能量
    private int maxMana;    // 本回合上限（每回合 +1，最多 MAX_MANA_CAP）
    private List<Card> hand;
    private List<VTuberCard> field;
    private Deck deck;
    private int fatigueDamage;  // 牌組空時每次抽牌的疲勞傷害（遞增）

    // 用於戰績顯示的 DB 欄位
    private int wins;
    private int losses;

    public static final int INITIAL_HP    = 30;
    public static final int MAX_MANA_CAP  = 10;
    public static final int FIELD_LIMIT   = 4;
    public static final int HAND_LIMIT    = 8;

    /** 對戰用建構子 */
    public Player(String username, Faction faction) {
        this.username = username;
        this.faction = faction;
        this.hp = INITIAL_HP;
        this.maxHp = INITIAL_HP;
        this.mana = 0;
        this.maxMana = 0;
        this.hand = new ArrayList<>();
        this.field = new ArrayList<>();
        this.fatigueDamage = 0;
    }

    /** 資料庫讀取用建構子（僅載入帳號與戰績） */
    public Player(int id, String username, int wins, int losses) {
        this.id = id;
        this.username = username;
        this.wins = wins;
        this.losses = losses;
        this.hand = new ArrayList<>();
        this.field = new ArrayList<>();
    }

    // === 回合管理 ===

    /** 回合開始：魔力 +1 並回滿，場上所有 VTuber 解鎖攻擊 */
    public void startTurn() {
        if (maxMana < MAX_MANA_CAP) maxMana++;
        mana = maxMana;  // 每回合魔力全部回滿
        for (VTuberCard v : field) {
            v.onTurnStart();  // 解除凍結或允許攻擊
        }
    }

    /**
     * 抽一張牌
     * 牌組為空時不抽牌，改為受疲勞傷害（每次 +2，遞增）
     */
    public Card drawCard() {
        if (deck == null || deck.isEmpty()) {
            fatigueDamage += 2;
            hp -= fatigueDamage;
            return null;
        }
        Card card = deck.draw();
        // 手牌超過上限時，多出的牌直接燒掉（不加入手牌）
        if (hand.size() < HAND_LIMIT) {
            hand.add(card);
        }
        return card;
    }

    /**
     * 對戰開始前重置所有戰鬥狀態
     * 資料庫載入的 Player 只有帳號與戰績，對戰前必須呼叫此方法初始化 HP/魔力/手牌/場地
     */
    public void initForBattle() {
        this.hp = INITIAL_HP;
        this.maxHp = INITIAL_HP;
        this.mana = 0;
        this.maxMana = 0;
        this.fatigueDamage = 0;
        this.hand = new ArrayList<>();
        this.field = new ArrayList<>();
    }

    /** 遊戲開始時抽起始手牌 */
    public void drawInitialHand(int count) {
        for (int i = 0; i < count; i++) {
            drawCard();
        }
    }

    // === 資源管理 ===
    public boolean canAfford(int cost) { return mana >= cost; }
    public void spendMana(int cost)    { this.mana -= cost; }
    public void gainMana(int amount)   { this.mana = Math.min(mana + amount, maxMana); }
    public void takeDamage(int damage) { this.hp -= damage; }

    /** 回血不超過最大 HP */
    public void heal(int amount) { this.hp = Math.min(hp + amount, maxHp); }
    public boolean isAlive()     { return hp > 0; }
    public boolean isFieldFull() { return field.size() >= FIELD_LIMIT; }

    /** 場上是否有嘲諷隨從（攻擊前須檢查） */
    public boolean hasTauntOnField() {
        return field.stream().anyMatch(VTuberCard::hasTaunt);
    }

    /** 取得場上所有嘲諷隨從（用於顯示必須攻擊哪一隻） */
    public List<VTuberCard> getTauntMinions() {
        return field.stream().filter(VTuberCard::hasTaunt).toList();
    }

    /**
     * 移除場上已死亡的隨從，回傳死亡列表
     * 注意：.toList() 回傳不可修改列表，呼叫端若不需合併此結果可直接捨棄
     */
    public List<VTuberCard> removeDeadMinions() {
        List<VTuberCard> dead = field.stream().filter(VTuberCard::isDead).toList();
        field.removeAll(dead);
        return dead;
    }

    // === Getters & Setters ===
    public int getId()            { return id; }
    public void setId(int id)     { this.id = id; }
    public String getUsername()   { return username; }
    public Faction getFaction()   { return faction; }
    public void setFaction(Faction f) { this.faction = f; }
    public int getHp()            { return hp; }
    public int getMaxHp()         { return maxHp; }
    public int getMana()          { return mana; }
    public int getMaxMana()       { return maxMana; }
    public List<Card> getHand()   { return hand; }
    public List<VTuberCard> getField() { return field; }
    public Deck getDeck()         { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }
    public int getWins()          { return wins; }
    public int getLosses()        { return losses; }
    public void setWins(int w)    { this.wins = w; }
    public void setLosses(int l)  { this.losses = l; }
    public int getFatigueDamage() { return fatigueDamage; }

    /** 勝率（總場次為 0 時回傳 0.0） */
    public double getWinRate() {
        int total = wins + losses;
        return total == 0 ? 0 : (double) wins / total * 100;
    }
}
