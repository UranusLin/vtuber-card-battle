package com.vcb.model;

import com.vcb.model.enums.*;

/**
 * 卡牌抽象基底類別
 * 所有卡牌（VTuber/才藝/應援）都繼承此類別，展示封裝與繼承的 OOP 原則
 */
public abstract class Card {
    protected int id;
    protected String name;
    protected Faction faction;
    protected CardType cardType;
    protected int manaCost;    // 使用所需 SC 能量
    protected Rarity rarity;
    protected String effectDesc;   // 給玩家看的效果說明文字
    protected String flavorText;   // 風味文字（故事背景）

    public Card() {}

    public Card(int id, String name, Faction faction, CardType cardType,
                int manaCost, Rarity rarity, String effectDesc, String flavorText) {
        this.id = id;
        this.name = name;
        this.faction = faction;
        this.cardType = cardType;
        this.manaCost = manaCost;
        this.rarity = rarity;
        this.effectDesc = effectDesc;
        this.flavorText = flavorText;
    }

    /**
     * 深拷貝 — 將圖鑑中的卡牌複製一份放入手牌
     * 目的：確保每張手牌的狀態（HP/ATK 等）相互獨立
     */
    public abstract Card copy();

    /** 場上顯示格式（簡短） */
    public abstract String display();

    /** 手牌中的完整顯示（含費用與效果） */
    public abstract String handDisplay();

    // === Getters ===
    public int getId()          { return id; }
    public String getName()     { return name; }
    public Faction getFaction() { return faction; }
    public CardType getCardType() { return cardType; }
    public int getManaCost()    { return manaCost; }
    public Rarity getRarity()   { return rarity; }
    public String getEffectDesc()  { return effectDesc; }
    public String getFlavorText()  { return flavorText; }
}
