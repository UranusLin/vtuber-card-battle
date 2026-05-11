package com.vcb.model;

import com.vcb.model.enums.*;

/**
 * 應援卡 — 裝備在場上的 VTuber 身上，提升攻擊力/生命值或賦予能力
 * 裝備後應援卡本身消失，加成直接寫入 VTuberCard
 */
public class SupportCard extends Card {
    private int attackBonus;         // 裝備後 ATK 增加量
    private int healthBonus;         // 裝備後 HP 增加量（同時增加上限）
    private Ability grantAbility;    // 賦予的特殊能力（可為 null）

    public SupportCard() { this.cardType = CardType.SUPPORT; }

    public SupportCard(int id, String name, Faction faction, int manaCost, Rarity rarity,
                       int attackBonus, int healthBonus, Ability grantAbility,
                       String effectDesc, String flavorText) {
        super(id, name, faction, CardType.SUPPORT, manaCost, rarity, effectDesc, flavorText);
        this.attackBonus = attackBonus;
        this.healthBonus = healthBonus;
        this.grantAbility = grantAbility;
    }

    /** 將加成直接套用到目標 VTuber（裝備邏輯） */
    public void equip(VTuberCard target) {
        target.buffAttack(attackBonus);
        target.buffHealth(healthBonus);
        if (grantAbility != null) {
            target.grantAbility(grantAbility);  // 例如：賦予嘲諷或衝鋒
        }
    }

    /** 深拷貝（應援卡無可變狀態） */
    @Override
    public Card copy() {
        return new SupportCard(id, name, faction, manaCost, rarity,
                attackBonus, healthBonus, grantAbility, effectDesc, flavorText);
    }

    @Override
    public String display() {
        return String.format("[%s] 💰%d %s", name, manaCost, effectDesc);
    }

    @Override
    public String handDisplay() {
        return String.format("%s %s  💰%d  %s", cardType.getIcon(), name, manaCost, effectDesc);
    }

    public int getAttackBonus()      { return attackBonus; }
    public int getHealthBonus()      { return healthBonus; }
    public Ability getGrantAbility() { return grantAbility; }
}
