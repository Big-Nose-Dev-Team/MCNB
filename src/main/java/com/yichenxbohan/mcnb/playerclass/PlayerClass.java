package com.yichenxbohan.mcnb.playerclass;

/**
 * 玩家職業枚舉
 * NONE = 尚未選擇
 */
public enum PlayerClass {

    NONE      ("未選擇",   "—",            0xFFAAAAAA, "尚未選擇任何職業"),
    SWORDSMAN ("劍鬥士",  "⚔",            0xFFFF8040, "近戰物理系，高爆發、高防禦"),
    MAGE      ("法師",    "✦",            0xFF9B6FFF, "魔法系，群體輸出與控制"),
    ARCHER    ("神射手",  "➶",            0xFF55FFFF, "遠程物理系，精準命中與暴擊"),
    CULTIVATOR("修練者",  "☯",            0xFF44FF88, "真氣流派，真實傷害與回復"),
    SUMMONER  ("召喚師",  "♦",            0xFFFFD700, "召喚系，以靈魂攻擊驅動僕從"),
    WHITE_MAGE ("白魔導", "✚",            0xFF88FFFF, "回復系，治療與神聖能量傷害"),
    ASSASSIN  ("刺客",   "†",            0xFFFF4488, "敏捷系，高閃避與暴擊傷害");

    /** 顯示名稱 */
    public final String displayName;

    /** 圖示符號 */
    public final String icon;

    /** HUD / UI 主色 */
    public final int color;

    /** 職業說明 */
    public final String description;

    PlayerClass(String displayName, String icon, int color, String description) {
        this.displayName = displayName;
        this.icon        = icon;
        this.color       = color;
        this.description = description;
    }

    /** 取得 「icon + 空格 + displayName」 的組合字串 */
    public String getLabel() {
        return icon + " " + displayName;
    }
}

