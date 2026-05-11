package com.vcb.model.enums;

/**
 * 卡牌類型列舉（對應資料庫 card_type 欄位）
 */
public enum CardType {
    VTUBER("🎤",  "VTuber 卡"),  // 可召喚到場上的隨從
    TALENT("✨",  "才藝卡"),      // 使用後立即生效的法術
    SUPPORT("🎁", "應援卡");     // 裝備到隨從身上的裝備

    private final String icon;
    private final String displayName;

    CardType(String icon, String displayName) {
        this.icon = icon;
        this.displayName = displayName;
    }

    public String getIcon()        { return icon; }
    public String getDisplayName() { return displayName; }

    /** 從資料庫字串轉換（必須完全匹配，大小寫不敏感） */
    public static CardType fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
