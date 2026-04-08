package com.yichenxbohan.mcnb.level;

import java.util.Locale;

/**
 * 五大屬性定義。
 */
public enum PlayerAttributeType {
    STRENGTH("力量", "增加物理攻擊", 0xFFFF8040, "物理攻擊", 2.5, false, -1),
    CONSTITUTION("體質", "增加防禦力", 0xFF40D8FF, "防禦力", 1.5, false, -1),
    POTENTIAL("潛能", "冷卻縮減，上限 75%", 0xFF9B6FFF, "冷卻縮減", 0.5, true, 75.0),
    INTELLIGENCE("智慧", "增加魔法攻擊", 0xFFE8D87A, "魔法攻擊", 2.5, false, -1),
    AGILITY("敏捷", "增加移動速度", 0xFF44FFAA, "移動速度", 0.15, true, -1);

    private static final PlayerAttributeType[] VALUES = values();

    private final String label;
    private final String description;
    private final int color;
    private final String bonusLabel;
    private final double bonusPerPoint;
    private final boolean percent;
    private final double cap;

    PlayerAttributeType(String label, String description, int color, String bonusLabel,
                        double bonusPerPoint, boolean percent, double cap) {
        this.label = label;
        this.description = description;
        this.color = color;
        this.bonusLabel = bonusLabel;
        this.bonusPerPoint = bonusPerPoint;
        this.percent = percent;
        this.cap = cap;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }

    public double getBonusPerPoint() {
        return bonusPerPoint;
    }

    public boolean isPercent() {
        return percent;
    }

    public double getCap() {
        return cap;
    }

    public double getBonusValue(int points) {
        double value = Math.max(0, points) * bonusPerPoint;
        if (cap >= 0) {
            value = Math.min(cap, value);
        }
        return value;
    }

    public String formatBonus(int points) {
        double value = getBonusValue(points);
        if (percent) {
            return String.format(Locale.ROOT, "+%.1f%% %s", value, bonusLabel);
        }
        return String.format(Locale.ROOT, "+%.1f %s", value, bonusLabel);
    }

    public static PlayerAttributeType fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return STRENGTH;
        }
        return VALUES[ordinal];
    }

    public static PlayerAttributeType[] orderedValues() {
        return VALUES.clone();
    }
}
