package com.vcb.model;

import java.util.*;

/**
 * 牌組 — 管理卡牌的洗牌與抽牌
 * 使用 LinkedList 以 O(1) 從頂部取牌（removeFirst）
 */
public class Deck {
    private int id;
    private String deckName;
    private String faction;
    private LinkedList<Card> cards;

    public Deck(String deckName, String faction) {
        this.deckName = deckName;
        this.faction = faction;
        this.cards = new LinkedList<>();
    }

    public Deck(int id, String deckName, String faction) {
        this(deckName, faction);
        this.id = id;
    }

    /**
     * 加入一張卡牌（組牌時呼叫）
     * 使用 copy() 確保每份牌組持有獨立的卡牌物件，不與圖鑑共用
     */
    public void addCard(Card card) {
        cards.add(card.copy());
    }

    /** 洗牌（Collections.shuffle 使用 Fisher-Yates 演算法） */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /** 從牌組頂部抽一張牌，牌組空時回傳 null */
    public Card draw() {
        if (cards.isEmpty()) return null;
        return cards.removeFirst();
    }

    public boolean isEmpty() { return cards.isEmpty(); }
    public int size()        { return cards.size(); }

    // === Getters ===
    public int getId()          { return id; }
    public void setId(int id)   { this.id = id; }
    public String getDeckName() { return deckName; }
    public String getFaction()  { return faction; }
    public List<Card> getCards() { return cards; }
}
