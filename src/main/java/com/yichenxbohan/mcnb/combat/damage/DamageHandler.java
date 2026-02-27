package com.yichenxbohan.mcnb.combat.damage;

import com.mojang.logging.LogUtils;
import com.yichenxbohan.mcnb.combat.CombatStats;
import com.yichenxbohan.mcnb.combat.DamageResult;
import com.yichenxbohan.mcnb.network.DamageNumberPacket;
import com.yichenxbohan.mcnb.network.ModNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import java.util.EnumMap;
import java.util.Random;

/**
 * 傷害處理器 - 處理所有傷害類型的計算和效果應用
 *
 * 傷害類型說明：
 * - 物理傷害 (PHYSICAL): 基礎傷害，受物理防禦影響
 * - 魔法傷害 (MAGIC): 受魔法抗性影響
 * - 能量傷害 (ENERGY): 受能量抗性影響
 * - 真實傷害 (TRUE): 無視一切防禦和減傷，直接造成傷害
 * - 靈魂傷害 (SOUL): 造成禁療效果，受靈魂抗性影響
 * - 混沌傷害 (CHAOS): 擁有極強穿透力（80%穿透），受混沌抗性影響
 * - 空間傷害 (SPATIAL): 造成緩速效果，受空間抗性影響
 * - 時空傷害 (TEMPORAL): 無視防禦力，但受傷害減免影響
 */
public class DamageHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();

    // ==================== 常數配置 ====================

    /** 防禦力計算常數 (越高，防禦力的收益越平緩) */
    private static final double DEFENSE_CONSTANT = 100.0;

    /** 最大傷害減免上限 */
    private static final double MAX_DAMAGE_REDUCTION = 0.80;

    /** 最小命中率 */
    private static final double MIN_HIT_CHANCE = 0.20;

    /** 最大暴擊率上限 */
    private static final double MAX_CRIT_CHANCE = 0.75;

    /** 混沌傷害的額外穿透率 */
    private static final double CHAOS_PENETRATION_BONUS = 0.80;

    /** 禁療效果持續時間 (tick，20 tick = 1 秒) */
    private static final int ANTI_HEAL_DURATION = 100; // 5秒

    /** 緩速效果持續時間 (tick) */
    private static final int SLOW_DURATION = 60; // 3秒

    /** 緩速效果等級 (0 = 等級1, 1 = 等級2, etc.) */
    private static final int SLOW_AMPLIFIER = 1; // 緩速 II

    /** 傷害數字顯示範圍 */
    private static final double DAMAGE_NUMBER_RADIUS = 64.0;

    // ==================== 主要計算方法 ====================

    /**
     * 計算傷害數據包的最終傷害
     *
     * @param packet 傷害數據包
     * @param attackerStats 攻擊者屬性
     * @param targetStats 目標屬性
     * @return 傷害結果
     */
    public static DamageResult calculate(DamagePacket packet, CombatStats attackerStats, CombatStats targetStats) {
        // 命中判定
        if (!checkHit(attackerStats, targetStats)) {
            return DamageResult.miss();
        }

        // 暴擊判定
        boolean isCrit = packet.canCrit && checkCrit(packet.critRate > 0 ? packet.critRate : (float) attackerStats.critChance);
        double critMultiplier = isCrit ? (packet.critMultiplier + attackerStats.critDamage) : 1.0;

        // 計算各類型傷害
        EnumMap<DamageTypeEx, Double> damageBreakdown = new EnumMap<>(DamageTypeEx.class);
        double totalDamage = 0;
        boolean hasAntiHeal = false;
        boolean hasSlow = false;

        for (DamageTypeEx type : DamageTypeEx.values()) {
            float rawDamage = packet.get(type);
            if (rawDamage <= 0) continue;

            double calculatedDamage = calculateSingleTypeDamage(
                rawDamage, type, attackerStats, targetStats,
                packet.penetration, critMultiplier, packet.damageBonus, packet.finalDamageMultiplier
            );

            if (calculatedDamage > 0) {
                damageBreakdown.put(type, calculatedDamage);
                totalDamage += calculatedDamage;

                // 檢查特殊效果
                if (type.antiHeal) hasAntiHeal = true;
                if (type.causesSlow) hasSlow = true;
            }
        }

        // 確保傷害不為負
        totalDamage = Math.max(0, totalDamage);

        return new DamageResult.Builder()
            .hit(true)
            .critical(isCrit)
            .totalDamage(totalDamage)
            .damageBreakdown(damageBreakdown)  // 添加傷害分解數據
            .antiHeal(hasAntiHeal, hasAntiHeal ? ANTI_HEAL_DURATION : 0)
            .slow(hasSlow, hasSlow ? SLOW_DURATION : 0)
            .build();
    }

    /**
     * 計算單一傷害類型的最終傷害
     */
    private static double calculateSingleTypeDamage(
            float rawDamage,
            DamageTypeEx type,
            CombatStats attacker,
            CombatStats target,
            float bonusPenetration,
            double critMultiplier,
            float damageBonus,
            float finalMultiplier
    ) {
        double damage = rawDamage;

        // 應用武器倍率
        damage *= attacker.weaponMultiplier;

        // 應用暴擊
        damage *= critMultiplier;

        // 根據傷害類型應用防禦計算
        if (!type.bypassDefense) {
            double resistance = target.getResistanceFor(type);
            double totalPenetration = attacker.penetration + bonusPenetration;

            // 混沌傷害有額外穿透
            if (type.highPenetration) {
                totalPenetration += resistance * CHAOS_PENETRATION_BONUS;
            }

            // 計算有效防禦
            double effectiveResistance = Math.max(0, resistance - totalPenetration);

            // 防禦減傷公式: damage = damage * (100 / (100 + effectiveResistance))
            damage = damage * (DEFENSE_CONSTANT / (DEFENSE_CONSTANT + effectiveResistance));
        }

        // 應用傷害加成
        damage *= (1 + attacker.damageBonus + damageBonus);

        // 應用傷害減免（真實傷害除外）
        if (!type.bypassReduction) {
            double reduction = clamp(target.damageReduction, 0, MAX_DAMAGE_REDUCTION);
            damage *= (1 - reduction);
        }

        // 應用最終傷害倍率
        damage *= attacker.finalDamageMultiplier * finalMultiplier;

        return Math.max(0, damage);
    }

    // ==================== 判定方法 ====================

    /**
     * 命中判定
     */
    private static boolean checkHit(CombatStats attacker, CombatStats target) {
        if (target.evasion <= 0) return true;

        double hitChance = attacker.penetration / (attacker.penetration + target.evasion + DEFENSE_CONSTANT);
        hitChance = clamp(hitChance, MIN_HIT_CHANCE, 1.0);

        return RANDOM.nextDouble() < hitChance;
    }

    /**
     * 暴擊判定
     */
    private static boolean checkCrit(float critChance) {
        double clampedCrit = clamp(critChance, 0, MAX_CRIT_CHANCE);
        return RANDOM.nextDouble() < clampedCrit;
    }

    // ==================== 效果應用方法 ====================

    /**
     * 將傷害結果應用到目標實體上
     *
     * @param result 傷害結果
     * @param target 目標實體
     * @param targetStats 目標戰鬥屬性（用於更新禁療狀態）
     */
    public static void applyEffects(DamageResult result, LivingEntity target, CombatStats targetStats) {
        if (!result.hit) return;

        // 應用禁療效果
        if (result.appliedAntiHeal && targetStats != null) {
            targetStats.antiHealDuration = Math.max(targetStats.antiHealDuration, result.antiHealDuration);
            LOGGER.debug("Applied anti-heal to {} for {} ticks", target.getName().getString(), result.antiHealDuration);
        }

        // 應用緩速效果
        if (result.appliedSlow) {
            target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                result.slowDuration,
                SLOW_AMPLIFIER,
                false,  // ambient
                true,   // visible
                true    // showIcon
            ));
            LOGGER.debug("Applied slowness to {} for {} ticks", target.getName().getString(), result.slowDuration);
        }
    }

    /**
     * 直接對實體造成傷害並應用效果
     *
     * @param packet 傷害數據包
     * @param attackerStats 攻擊者屬性
     * @param targetStats 目標屬性
     * @return 傷害結果
     */
    public static DamageResult dealDamage(DamagePacket packet, CombatStats attackerStats, CombatStats targetStats) {
        DamageResult result = calculate(packet, attackerStats, targetStats);

        if (result.hit && result.damage > 0) {
            // 對目標造成實際傷害
            if (packet.target != null) {
                // 使用原版傷害系統造成傷害
                packet.target.hurt(packet.target.damageSources().generic(), (float) result.damage);

                // 應用特殊效果
                applyEffects(result, packet.target, targetStats);

                // 發送傷害數字到客戶端
                sendDamageNumbers(packet.target, result);
            }
        }

        return result;
    }

    /**
     * 發送傷害數字到附近的客戶端
     */
    private static void sendDamageNumbers(LivingEntity target, DamageResult result) {
        if (target.level() instanceof ServerLevel serverLevel) {
            // 將傷害分解轉換為正確的類型
            EnumMap<DamageTypeEx, Double> damages = new EnumMap<>(DamageTypeEx.class);
            for (var entry : result.damageBreakdown.entrySet()) {
                damages.put(entry.getKey(), entry.getValue());
            }

            // 如果沒有分解數據，則使用總傷害作為通用傷害
            if (damages.isEmpty()) {
                damages.put(DamageTypeEx.PHYSICAL, result.damage);
            }

            // 創建並發送傷害數字數據包
            DamageNumberPacket numberPacket = new DamageNumberPacket(
                target.getX(),
                target.getY() + target.getBbHeight(),
                target.getZ(),
                damages,
                result.critical
            );

            // 發送給追蹤該實體的所有玩家
            ModNetworking.sendToAllTracking(numberPacket, target);
        }
    }

    /**
     * 發送單一類型的傷害數字
     */
    public static void sendDamageNumber(LivingEntity target, double damage, DamageTypeEx type, boolean isCrit) {
        if (target.level() instanceof ServerLevel) {
            LOGGER.debug("Sending damage number: {} {} damage to {} (crit: {})",
                damage, type.name(), target.getName().getString(), isCrit);

            DamageNumberPacket packet = new DamageNumberPacket(
                target.getX(),
                target.getY() + target.getBbHeight(),
                target.getZ(),
                damage,
                type,
                isCrit
            );
            ModNetworking.sendToAllTracking(packet, target);
        } else {
            LOGGER.warn("Cannot send damage number: target not in ServerLevel");
        }
    }

    // ==================== 便捷方法 ====================

    /**
     * 快速創建並計算物理傷害
     */
    public static DamageResult dealPhysicalDamage(LivingEntity attacker, LivingEntity target,
                                                   float damage, CombatStats attackerStats, CombatStats targetStats) {
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(DamageTypeEx.PHYSICAL, damage)
            .withCrit((float) attackerStats.critChance, (float) (1.5 + attackerStats.critDamage))
            .withPenetration((float) attackerStats.penetration);

        return dealDamage(packet, attackerStats, targetStats);
    }

    /**
     * 快速創建並計算魔法傷害
     */
    public static DamageResult dealMagicDamage(LivingEntity attacker, LivingEntity target,
                                                float damage, CombatStats attackerStats, CombatStats targetStats) {
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(DamageTypeEx.MAGIC, damage)
            .withCrit((float) attackerStats.critChance, (float) (1.5 + attackerStats.critDamage))
            .withPenetration((float) attackerStats.penetration);

        return dealDamage(packet, attackerStats, targetStats);
    }

    /**
     * 快速創建並計算真實傷害（無視一切防禦和減傷）
     */
    public static DamageResult dealTrueDamage(LivingEntity attacker, LivingEntity target,
                                               float damage, CombatStats attackerStats, CombatStats targetStats) {
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(DamageTypeEx.TRUE, damage);
        packet.canCrit = false; // 真實傷害通常不暴擊

        return dealDamage(packet, attackerStats, targetStats);
    }

    /**
     * 快速創建並計算靈魂傷害（造成禁療）
     */
    public static DamageResult dealSoulDamage(LivingEntity attacker, LivingEntity target,
                                               float damage, CombatStats attackerStats, CombatStats targetStats) {
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(DamageTypeEx.SOUL, damage)
            .withCrit((float) attackerStats.critChance, (float) (1.5 + attackerStats.critDamage))
            .withPenetration((float) attackerStats.penetration);

        return dealDamage(packet, attackerStats, targetStats);
    }

    /**
     * 快速創建並計算混沌傷害（極強穿透）
     */
    public static DamageResult dealChaosDamage(LivingEntity attacker, LivingEntity target,
                                                float damage, CombatStats attackerStats, CombatStats targetStats) {
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(DamageTypeEx.CHAOS, damage)
            .withCrit((float) attackerStats.critChance, (float) (1.5 + attackerStats.critDamage))
            .withPenetration((float) attackerStats.penetration);

        return dealDamage(packet, attackerStats, targetStats);
    }

    /**
     * 快速創建並計算空間傷害（造成緩速）
     */
    public static DamageResult dealSpatialDamage(LivingEntity attacker, LivingEntity target,
                                                  float damage, CombatStats attackerStats, CombatStats targetStats) {
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(DamageTypeEx.SPATIAL, damage)
            .withCrit((float) attackerStats.critChance, (float) (1.5 + attackerStats.critDamage))
            .withPenetration((float) attackerStats.penetration);

        return dealDamage(packet, attackerStats, targetStats);
    }

    /**
     * 快速創建並計算時空傷害（無視防禦力）
     */
    public static DamageResult dealTemporalDamage(LivingEntity attacker, LivingEntity target,
                                                   float damage, CombatStats attackerStats, CombatStats targetStats) {
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(DamageTypeEx.TEMPORAL, damage)
            .withCrit((float) attackerStats.critChance, (float) (1.5 + attackerStats.critDamage))
            .withPenetration((float) attackerStats.penetration);

        return dealDamage(packet, attackerStats, targetStats);
    }

    // ==================== 工具方法 ====================

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
