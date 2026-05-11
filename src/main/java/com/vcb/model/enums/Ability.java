package com.vcb.model.enums;

/**
 * VTuber 卡的特殊能力列舉
 * 每張 VTuberCard 最多擁有一種能力
 */
public enum Ability {
    CHARGE("衝鋒",   "召喚當回合即可攻擊"),
    TAUNT("嘲諷",    "敵方必須優先攻擊此隨從"),
    BATTLE_CRY("戰吼", "進場時觸發一次性效果"),
    DEATH_RATTLE("亡語", "死亡時觸發效果");

    private final String displayName;
    private final String description;

    Ability(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /** 從字串解析，null 或空白回傳 null（表示無特殊能力） */
    public static Ability fromString(String s) {
        if (s == null || s.isBlank()) return null;
        try { return valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}
