package com.yichenxbohan.mcnb.combat;

import com.yichenxbohan.mcnb.combat.damage.DamageTypeEx;

import java.util.EnumMap;

/**
 * 傷害結果類 - 封裝傷害計算的完整結果
 */
public class DamageResult {
    /** 是否命中 */
    public final boolean hit;

    /** 是否暴擊 */
    public final boolean critical;

    /** 最終總傷害 */
    public final double damage;

    /** 各類型傷害的分解數值 */
    public final EnumMap<DamageTypeEx, Double> damageBreakdown;

    /** 是否造成禁療效果 */
    public final boolean appliedAntiHeal;

    /** 是否造成緩速效果 */
    public final boolean appliedSlow;

    /** 禁療持續時間（tick） */
    public final int antiHealDuration;

    /** 緩速持續時間（tick） */
    public final int slowDuration;

    /**
     * 簡單構造器（向後兼容）
     */
    public DamageResult(boolean hit, boolean critical, double damage) {
        this.hit = hit;
        this.critical = critical;
        this.damage = damage;
        this.damageBreakdown = new EnumMap<>(DamageTypeEx.class);
        this.appliedAntiHeal = false;
        this.appliedSlow = false;
        this.antiHealDuration = 0;
        this.slowDuration = 0;
    }

    /**
     * 完整構造器
     */
    public DamageResult(boolean hit, boolean critical, double damage,
                        EnumMap<DamageTypeEx, Double> damageBreakdown,
                        boolean appliedAntiHeal, boolean appliedSlow,
                        int antiHealDuration, int slowDuration) {
        this.hit = hit;
        this.critical = critical;
        this.damage = damage;
        this.damageBreakdown = damageBreakdown != null ? damageBreakdown : new EnumMap<>(DamageTypeEx.class);
        this.appliedAntiHeal = appliedAntiHeal;
        this.appliedSlow = appliedSlow;
        this.antiHealDuration = antiHealDuration;
        this.slowDuration = slowDuration;
    }

    /**
     * 獲取指定類型的傷害
     */
    public double getDamageOfType(DamageTypeEx type) {
        return damageBreakdown.getOrDefault(type, 0.0);
    }

    /**
     * 創建一個未命中的結果
     */
    public static DamageResult miss() {
        return new DamageResult(false, false, 0);
    }

    /**
     * 建造者類
     */
    public static class Builder {
        private boolean hit = true;
        private boolean critical = false;
        private double totalDamage = 0;
        private EnumMap<DamageTypeEx, Double> breakdown = new EnumMap<>(DamageTypeEx.class);
        private boolean antiHeal = false;
        private boolean slow = false;
        private int antiHealDuration = 0;
        private int slowDuration = 0;

        public Builder hit(boolean hit) {
            this.hit = hit;
            return this;
        }

        public Builder critical(boolean critical) {
            this.critical = critical;
            return this;
        }

        public Builder totalDamage(double damage) {
            this.totalDamage = damage;
            return this;
        }

        public Builder addDamage(DamageTypeEx type, double damage) {
            this.breakdown.put(type, damage);
            this.totalDamage += damage;
            return this;
        }

        /**
         * 設置整個傷害分解映射
         */
        public Builder damageBreakdown(EnumMap<DamageTypeEx, Double> breakdown) {
            this.breakdown = breakdown;
            return this;
        }

        public Builder antiHeal(boolean apply, int duration) {
            this.antiHeal = apply;
            this.antiHealDuration = duration;
            return this;
        }

        public Builder slow(boolean apply, int duration) {
            this.slow = apply;
            this.slowDuration = duration;
            return this;
        }

        public DamageResult build() {
            return new DamageResult(hit, critical, totalDamage, breakdown,
                                   antiHeal, slow, antiHealDuration, slowDuration);
        }
    }
}
