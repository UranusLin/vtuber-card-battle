package com.vcb.model;

import com.vcb.model.enums.*;

/**
 * VTuber 卡 — 可召喚到場上，具有攻擊力和生命值
 * 繼承 Card 抽象類別，實作 copy()、display()、handDisplay()
 */
public class VTuberCard extends Card {
    private int attack;
    private int health;
    private int maxHealth;     // 用於計算回血上限
    private Ability ability;
    private String effectType; // 回合結束效果類型（如 HEAL_ALL_ALLY）
    private int effectValue;
    private boolean canAttack; // 剛召喚時為 false，除非有衝鋒
    private boolean frozen;    // 凍結狀態：下回合開始時解除，但該回合無法攻擊

    public VTuberCard() { this.cardType = CardType.VTUBER; }

    public VTuberCard(int id, String name, Faction faction, int manaCost, Rarity rarity,
                      int attack, int health, Ability ability, String effectType,
                      int effectValue, String effectDesc, String flavorText) {
        super(id, name, faction, CardType.VTUBER, manaCost, rarity, effectDesc, flavorText);
        this.attack = attack;
        this.health = health;
        this.maxHealth = health;
        this.ability = ability;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.canAttack = (ability == Ability.CHARGE);  // 衝鋒：召喚當回合就能攻擊
        this.frozen = false;
    }

    /** 攻擊另一隻 VTuber（互相傷害） */
    public void attackTarget(VTuberCard target) {
        target.takeDamage(this.attack);
        this.takeDamage(target.getAttack());
        this.canAttack = false;
    }

    /** 攻擊英雄（回傳傷害值供 Player.takeDamage 使用） */
    public int attackHero() {
        this.canAttack = false;
        return this.attack;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
    }

    /** 回血不超過召喚時的原始最大 HP */
    public void heal(int amount) {
        this.health = Math.min(this.health + amount, this.maxHealth);
    }

    public boolean isDead() { return health <= 0; }

    // === 回合管理 ===

    /**
     * 回合開始時呼叫
     * 凍結狀態下：解除凍結但該回合仍不能攻擊
     * 正常狀態下：允許攻擊
     */
    public void onTurnStart() {
        if (frozen) {
            frozen = false;
            canAttack = false;  // 被凍結的回合不能攻擊
        } else {
            canAttack = true;
        }
    }

    /** 凍結：立即失去攻擊權，下回合開始時解除 */
    public void freeze() {
        this.frozen = true;
        this.canAttack = false;
    }

    // === Buff ===
    public void buffAttack(int amount)  { this.attack += amount; }

    /** HP buff 同時增加上限，確保回血能達到 buff 後的新上限 */
    public void buffHealth(int amount) {
        this.health += amount;
        this.maxHealth += amount;
    }

    public void grantAbility(Ability newAbility) {
        this.ability = newAbility;
        if (newAbility == Ability.CHARGE) this.canAttack = true;  // 賦予衝鋒時立即解鎖攻擊
    }

    /** 深拷貝：從圖鑑複製一份到手牌，避免共用同一物件 */
    @Override
    public Card copy() {
        return new VTuberCard(id, name, faction, manaCost, rarity,
                attack, health, ability, effectType, effectValue, effectDesc, flavorText);
    }

    /** 場上顯示格式：[名稱 ATK/HP 能力 凍結] */
    @Override
    public String display() {
        String abilityStr = (ability != null) ? " [" + ability.getDisplayName() + "]" : "";
        String frozenStr = frozen ? "❄️" : "";
        return String.format("[%s %d/%d%s%s]", name, attack, health, abilityStr, frozenStr);
    }

    /** 手牌顯示格式（含費用） */
    @Override
    public String handDisplay() {
        String abilityStr = (ability != null) ? " " + ability.getDisplayName() : "";
        return String.format("%s %s  💰%d  ATK:%d HP:%d%s",
                cardType.getIcon(), name, manaCost, attack, health, abilityStr);
    }

    // === Getters & Setters ===
    public int getAttack()       { return attack; }
    public int getHealth()       { return health; }
    public int getMaxHealth()    { return maxHealth; }
    public Ability getAbility()  { return ability; }
    public String getEffectType()  { return effectType; }
    public int getEffectValue()    { return effectValue; }
    public boolean canAttack()     { return canAttack && !frozen; }  // 凍結時強制不能攻擊
    public boolean isFrozen()      { return frozen; }
    public boolean hasTaunt()      { return ability == Ability.TAUNT; }
    public void setCanAttack(boolean b) { this.canAttack = b; }
    public void setAttack(int a)   { this.attack = a; }
    public void setHealth(int h)   { this.health = h; }
}
