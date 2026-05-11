package com.vcb.model.enums;

/**
 * 卡牌稀有度列舉（對應資料庫 rarity 整數欄位）
 */
public enum Rarity {
    COMMON(1,    "★"),      // 普通
    RARE(2,      "★★"),     // 稀有
    EPIC(3,      "★★★"),    // 史詩
    LEGENDARY(4, "★★★★");  // 傳說（每副牌最多 1 張）

    private final int stars;
    private final String display;

    Rarity(int stars, String display) {
        this.stars = stars;
        this.display = display;
    }

    public int getStars()    { return stars; }
    public String getDisplay() { return display; }

    /** 從資料庫整數欄位轉換，找不到時預設 COMMON */
    public static Rarity fromInt(int i) {
        for (Rarity r : values()) {
            if (r.stars == i) return r;
        }
        return COMMON;
    }
}
