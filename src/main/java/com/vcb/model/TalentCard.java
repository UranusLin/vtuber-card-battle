package com.vcb.model;

import com.vcb.model.enums.*;

/**
 * 才藝卡 — 使用後立即發動效果，用完即棄（不留在場上）
 * effectType 決定效果種類，targetType 決定作用對象
 */
public class TalentCard extends Card {
    private String effectType;  // DAMAGE / HEAL / DRAW / FREEZE / BUFF_ATK / BUFF_HP ...
    private int effectValue;    // 效果數值（傷害量、回血量、抽牌數等）
    private String targetType;  // HERO / SINGLE_ENEMY / ALL_ENEMIES / SINGLE_ALLY / ALL_ALLIES

    public TalentCard() { this.cardType = CardType.TALENT; }

    public TalentCard(int id, String name, Faction faction, int manaCost, Rarity rarity,
                      String effectType, int effectValue, String targetType,
                      String effectDesc, String flavorText) {
        super(id, name, faction, CardType.TALENT, manaCost, rarity, effectDesc, flavorText);
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.targetType = targetType;
    }

    /** 深拷貝（才藝卡無狀態，直接以相同參數建立新物件即可） */
    @Override
    public Card copy() {
        return new TalentCard(id, name, faction, manaCost, rarity,
                effectType, effectValue, targetType, effectDesc, flavorText);
    }

    @Override
    public String display() {
        return String.format("[%s] 💰%d %s", name, manaCost, effectDesc);
    }

    @Override
    public String handDisplay() {
        return String.format("%s %s  💰%d  %s", cardType.getIcon(), name, manaCost, effectDesc);
    }

    public String getEffectType()  { return effectType; }
    public int getEffectValue()    { return effectValue; }
    public String getTargetType()  { return targetType; }
}
