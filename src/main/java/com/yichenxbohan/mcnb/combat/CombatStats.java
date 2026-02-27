package com.yichenxbohan.mcnb.combat;

/**
 * 戰鬥屬性類 - 定義攻擊、防禦和恢復相關的所有屬性
 */
public class CombatStats {
    // ==================== 攻擊屬性 ====================

    /** 物理攻擊力 */
    public double physicalAttack;

    /** 魔法攻擊力 */
    public double magicAttack;

    /** 真實攻擊力 */
    public double trueAttack;

    /** 能量攻擊力 */
    public double energyAttack;

    /** 靈魂攻擊力 */
    public double soulAttack;

    /** 混沌攻擊力 */
    public double chaosAttack;

    /** 空間攻擊力 */
    public double spatialAttack;

    /** 時空攻擊力 */
    public double temporalAttack;

    /** 武器倍率 */
    public double weaponMultiplier = 1.0;

    /** 穿透值 */
    public double penetration;

    /** 暴擊率 (0.0 - 1.0) */
    public double critChance;

    /** 暴擊傷害加成 */
    public double critDamage;

    /** 傷害加成 */
    public double damageBonus;

    /** 最終傷害倍率 */
    public double finalDamageMultiplier = 1.0;

    // ==================== 防禦屬性 ====================

    /** 物理防禦力 */
    public double defense;

    /** 魔法抗性 */
    public double magicResistance;

    /** 能量抗性 */
    public double energyResistance;

    /** 靈魂抗性 */
    public double soulResistance;

    /** 混沌抗性 */
    public double chaosResistance;

    /** 空間抗性 */
    public double spatialResistance;

    /** 時空抗性 */
    public double temporalResistance;

    /** 傷害減免 (0.0 - 1.0，上限通常為 0.8) */
    public double damageReduction;

    /** 閃避率 */
    public double evasion;

    // ==================== 恢復屬性 ====================

    /** 治療加成 */
    public double healingBonus;

    /** 生命回復 */
    public double regen;

    // ==================== 特殊狀態 ====================

    /** 禁療持續時間（tick） */
    public int antiHealDuration;

    /** 緩速持續時間（tick） */
    public int slowDuration;

    /**
     * 根據傷害類型獲取對應的抗性值
     */
    public double getResistanceFor(com.yichenxbohan.mcnb.combat.damage.DamageTypeEx type) {
        return switch (type) {
            case PHYSICAL -> defense;
            case MAGIC -> magicResistance;
            case ENERGY -> energyResistance;
            case SOUL -> soulResistance;
            case CHAOS -> chaosResistance;
            case SPATIAL -> spatialResistance;
            case TEMPORAL -> temporalResistance;
            case TRUE -> 0; // 真實傷害無視所有抗性
        };
    }

    /**
     * 根據傷害類型獲取對應的攻擊力
     */
    public double getAttackFor(com.yichenxbohan.mcnb.combat.damage.DamageTypeEx type) {
        return switch (type) {
            case PHYSICAL -> physicalAttack;
            case MAGIC -> magicAttack;
            case ENERGY -> energyAttack;
            case SOUL -> soulAttack;
            case CHAOS -> chaosAttack;
            case SPATIAL -> spatialAttack;
            case TEMPORAL -> temporalAttack;
            case TRUE -> trueAttack;
        };
    }
}
