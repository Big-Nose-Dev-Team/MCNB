package com.yichenxbohan.mcnb.combat.damage;

/**
 * 擴展傷害類型枚舉
 * 每種傷害類型都有其獨特的特性和效果
 */
public enum DamageTypeEx {
    /**
     * 物理傷害 - 基礎傷害類型，受防禦力影響
     */
    PHYSICAL(false, false, false, false, false),

    /**
     * 魔法傷害 - 受魔法抗性影響
     */
    MAGIC(false, false, false, false, false),

    /**
     * 能量傷害 - 受能量抗性影響
     */
    ENERGY(false, false, false, false, false),

    /**
     * 真實傷害 - 無視一切防禦和減傷，直接造成傷害
     */
    TRUE(true, true, false, false, false),

    /**
     * 靈魂傷害 - 造成禁療效果
     */
    SOUL(false, false, true, false, false),

    /**
     * 混沌傷害 - 擁有極強穿透力，大幅無視防禦
     */
    CHAOS(false, false, false, true, false),

    /**
     * 空間傷害 - 受到傷害時會造成緩速效果
     */
    SPATIAL(false, false, false, false, true),

    /**
     * 時空傷害 - 無視防禦力，但受傷害減免影響
     */
    TEMPORAL(true, false, false, false, false);

    /** 是否無視防禦力 */
    public final boolean bypassDefense;

    /** 是否無視傷害減免 */
    public final boolean bypassReduction;

    /** 是否造成禁療效果 */
    public final boolean antiHeal;

    /** 是否擁有極強穿透力 */
    public final boolean highPenetration;

    /** 是否造成緩速效果 */
    public final boolean causesSlow;

    DamageTypeEx(boolean bypassDefense, boolean bypassReduction, boolean antiHeal,
                 boolean highPenetration, boolean causesSlow) {
        this.bypassDefense = bypassDefense;
        this.bypassReduction = bypassReduction;
        this.antiHeal = antiHeal;
        this.highPenetration = highPenetration;
        this.causesSlow = causesSlow;
    }

    /**
     * 獲取該傷害類型的顯示名稱（用於本地化 key）
     */
    public String getTranslationKey() {
        return "mcnb.damage_type." + this.name().toLowerCase();
    }
}
