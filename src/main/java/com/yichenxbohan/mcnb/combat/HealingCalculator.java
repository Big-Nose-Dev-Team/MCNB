package com.yichenxbohan.mcnb.combat;

/**
 * 治療計算器 - 處理所有治療相關的計算
 * 支援禁療效果（靈魂傷害造成）
 */
public class HealingCalculator {

    /**
     * 計算實際治療量
     * 如果目標處於禁療狀態，治療量會大幅降低或完全無效
     *
     * @param baseHeal 基礎治療量
     * @param target 目標戰鬥屬性
     * @return 實際治療量
     */
    public static double calculateHeal(double baseHeal, CombatStats target) {
        // 檢查禁療效果
        if (target.antiHealDuration > 0) {
            // 禁療期間治療效果降低 80%
            return baseHeal * (1 + target.healingBonus) * 0.2;
        }

        return baseHeal * (1 + target.healingBonus);
    }

    /**
     * 計算實際治療量（完全禁療版本）
     *
     * @param baseHeal 基礎治療量
     * @param target 目標戰鬥屬性
     * @param fullAntiHeal 如果為 true，禁療期間完全無法治療
     * @return 實際治療量
     */
    public static double calculateHeal(double baseHeal, CombatStats target, boolean fullAntiHeal) {
        // 檢查禁療效果
        if (target.antiHealDuration > 0) {
            if (fullAntiHeal) {
                return 0; // 完全禁療
            }
            // 禁療期間治療效果降低 80%
            return baseHeal * (1 + target.healingBonus) * 0.2;
        }

        return baseHeal * (1 + target.healingBonus);
    }

    /**
     * 獲取每秒生命回復量
     * 禁療狀態下生命回復也會受影響
     *
     * @param target 目標戰鬥屬性
     * @return 每秒回復量
     */
    public static double getRegenPerSecond(CombatStats target) {
        if (target.antiHealDuration > 0) {
            // 禁療期間生命回復降低 80%
            return target.regen * 0.2;
        }
        return target.regen;
    }

    /**
     * 更新禁療和緩速持續時間（每 tick 調用一次）
     *
     * @param stats 戰鬥屬性
     */
    public static void tickEffects(CombatStats stats) {
        if (stats.antiHealDuration > 0) {
            stats.antiHealDuration--;
        }
        if (stats.slowDuration > 0) {
            stats.slowDuration--;
        }
    }

    /**
     * 檢查目標是否處於禁療狀態
     */
    public static boolean isAntiHealed(CombatStats target) {
        return target.antiHealDuration > 0;
    }

    /**
     * 獲取禁療剩餘時間（秒）
     */
    public static double getAntiHealRemainingSeconds(CombatStats target) {
        return target.antiHealDuration / 20.0;
    }
}
