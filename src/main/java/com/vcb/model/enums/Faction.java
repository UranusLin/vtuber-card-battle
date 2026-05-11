package com.vcb.model.enums;

/**
 * 卡牌陣營列舉
 * 三角剋制：極深空 → 瀕臨絕種 → 瑟拉斯蒂歐 → 極深空（×1.5）
 */
public enum Faction {
    DEEP_SPACE("極深空計畫", "✨"),
    RESCUTE("瀕臨絕種團",   "🐾"),
    CELESTIAL("瑟拉斯蒂歐", "🌤️"),
    TIMAEUS("諦覓司",       "⚔️"),
    PARADOX("五律悖反",     "🔮"),
    NEUTRAL("中立",         "🎴");

    private final String displayName;
    private final String icon;

    Faction(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon()        { return icon; }

    /**
     * 計算陣營剋制的傷害倍率
     * 相剋：×1.5  |  被剋：×0.75  |  其他（含中立）：×1.0
     */
    public double getAdvantageMultiplier(Faction defender) {
        if (this == NEUTRAL || defender == NEUTRAL) return 1.0;

        // 三角相剋（順時針方向 ×1.5）
        if (this == DEEP_SPACE && defender == RESCUTE)   return 1.5;
        if (this == RESCUTE    && defender == CELESTIAL) return 1.5;
        if (this == CELESTIAL  && defender == DEEP_SPACE) return 1.5;

        // 反向（被剋 ×0.75）
        if (this == RESCUTE    && defender == DEEP_SPACE)  return 0.75;
        if (this == CELESTIAL  && defender == RESCUTE)     return 0.75;
        if (this == DEEP_SPACE && defender == CELESTIAL)   return 0.75;

        return 1.0;  // TIMAEUS / PARADOX 無剋制關係
    }

    /** 從資料庫字串轉換（不區分大小寫，找不到時回傳 NEUTRAL） */
    public static Faction fromString(String s) {
        for (Faction f : values()) {
            if (f.name().equalsIgnoreCase(s)) return f;
        }
        return NEUTRAL;
    }
}
