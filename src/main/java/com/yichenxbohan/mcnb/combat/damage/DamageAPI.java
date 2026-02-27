package com.yichenxbohan.mcnb.combat.damage;

import com.yichenxbohan.mcnb.combat.CombatStats;
import com.yichenxbohan.mcnb.combat.DamageResult;
import com.yichenxbohan.mcnb.combat.ModDamageSources;
import com.yichenxbohan.mcnb.combat.StatsProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 傷害 API - 提供簡單易用的方法來造成各種類型的傷害
 *
 * 使用範例：
 * <pre>
 * // 造成 50 點物理傷害
 * DamageAPI.dealDamage(attacker, target, DamageTypeEx.PHYSICAL, 50);
 *
 * // 造成 30 點魔法傷害
 * DamageAPI.dealDamage(attacker, target, DamageTypeEx.MAGIC, 30);
 *
 * // 造成複合傷害（物理 + 靈魂）
 * DamageAPI.dealMultipleDamage(attacker, target, Map.of(
 *     DamageTypeEx.PHYSICAL, 40f,
 *     DamageTypeEx.SOUL, 20f  // 會造成禁療
 * ));
 *
 * // 使用自定義屬性造成傷害
 * CombatStats customStats = new CombatStats();
 * customStats.critChance = 0.5;  // 50% 暴擊率
 * customStats.critDamage = 1.0;  // 100% 暴擊傷害
 * DamageAPI.dealDamageWithStats(attacker, target, DamageTypeEx.CHAOS, 100, customStats);
 * </pre>
 */
public class DamageAPI {

    // ==================== 基礎傷害方法 ====================

    /**
     * 造成單一類型傷害（使用實體的戰鬥屬性）
     *
     * @param attacker 攻擊者
     * @param target 目標
     * @param type 傷害類型
     * @param amount 傷害數值
     * @return 傷害結果
     */
    public static DamageResult dealDamage(LivingEntity attacker, LivingEntity target,
                                           DamageTypeEx type, float amount) {
        CombatStats atkStats = StatsProvider.get(attacker);
        CombatStats defStats = StatsProvider.get(target);
        return dealDamageWithStats(attacker, target, type, amount, atkStats, defStats);
    }

    /**
     * 造成單一類型傷害（使用自定義攻擊屬性）
     */
    public static DamageResult dealDamageWithStats(LivingEntity attacker, LivingEntity target,
                                                    DamageTypeEx type, float amount,
                                                    CombatStats attackerStats) {
        CombatStats defStats = StatsProvider.get(target);
        return dealDamageWithStats(attacker, target, type, amount, attackerStats, defStats);
    }

    /**
     * 造成單一類型傷害（使用完全自定義的屬性）
     */
    public static DamageResult dealDamageWithStats(LivingEntity attacker, LivingEntity target,
                                                    DamageTypeEx type, float amount,
                                                    CombatStats attackerStats, CombatStats targetStats) {
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(type, amount)
            .withCrit((float) attackerStats.critChance, (float) (1.5 + attackerStats.critDamage))
            .withPenetration((float) attackerStats.penetration)
            .withDamageBonus((float) attackerStats.damageBonus)
            .withFinalMultiplier((float) attackerStats.finalDamageMultiplier);

        return executePacket(packet, attackerStats, targetStats);
    }

    // ==================== 複合傷害方法 ====================

    /**
     * 造成多種類型傷害
     *
     * @param attacker 攻擊者
     * @param target 目標
     * @param damages 傷害類型和數值的映射
     * @return 傷害結果
     */
    public static DamageResult dealMultipleDamage(LivingEntity attacker, LivingEntity target,
                                                   java.util.Map<DamageTypeEx, Float> damages) {
        CombatStats atkStats = StatsProvider.get(attacker);
        CombatStats defStats = StatsProvider.get(target);

        DamagePacket packet = new DamagePacket(attacker, target)
            .withCrit((float) atkStats.critChance, (float) (1.5 + atkStats.critDamage))
            .withPenetration((float) atkStats.penetration)
            .withDamageBonus((float) atkStats.damageBonus)
            .withFinalMultiplier((float) atkStats.finalDamageMultiplier);

        for (var entry : damages.entrySet()) {
            packet.add(entry.getKey(), entry.getValue());
        }

        return executePacket(packet, atkStats, defStats);
    }

    // ==================== 快捷方法 ====================

    /**
     * 造成物理傷害
     */
    public static DamageResult physical(LivingEntity attacker, LivingEntity target, float amount) {
        return dealDamage(attacker, target, DamageTypeEx.PHYSICAL, amount);
    }

    /**
     * 造成魔法傷害
     */
    public static DamageResult magic(LivingEntity attacker, LivingEntity target, float amount) {
        return dealDamage(attacker, target, DamageTypeEx.MAGIC, amount);
    }

    /**
     * 造成真實傷害（無視一切防禦和減傷）
     */
    public static DamageResult trueDamage(LivingEntity attacker, LivingEntity target, float amount) {
        CombatStats atkStats = StatsProvider.get(attacker);
        CombatStats defStats = StatsProvider.get(target);

        DamagePacket packet = new DamagePacket(attacker, target)
            .set(DamageTypeEx.TRUE, amount);
        packet.canCrit = false; // 真實傷害不暴擊

        return executePacket(packet, atkStats, defStats);
    }

    /**
     * 造成能量傷害
     */
    public static DamageResult energy(LivingEntity attacker, LivingEntity target, float amount) {
        return dealDamage(attacker, target, DamageTypeEx.ENERGY, amount);
    }

    /**
     * 造成靈魂傷害（造成禁療效果）
     */
    public static DamageResult soul(LivingEntity attacker, LivingEntity target, float amount) {
        return dealDamage(attacker, target, DamageTypeEx.SOUL, amount);
    }

    /**
     * 造成混沌傷害（極強穿透）
     */
    public static DamageResult chaos(LivingEntity attacker, LivingEntity target, float amount) {
        return dealDamage(attacker, target, DamageTypeEx.CHAOS, amount);
    }

    /**
     * 造成空間傷害（造成緩速效果）
     */
    public static DamageResult spatial(LivingEntity attacker, LivingEntity target, float amount) {
        return dealDamage(attacker, target, DamageTypeEx.SPATIAL, amount);
    }

    /**
     * 造成時空傷害（無視防禦力）
     */
    public static DamageResult temporal(LivingEntity attacker, LivingEntity target, float amount) {
        return dealDamage(attacker, target, DamageTypeEx.TEMPORAL, amount);
    }

    // ==================== 內部方法 ====================

    /**
     * 執行傷害數據包
     */
    private static DamageResult executePacket(DamagePacket packet, CombatStats atkStats, CombatStats defStats) {
        DamageResult result = DamageHandler.calculate(packet, atkStats, defStats);

        if (result.hit && result.damage > 0 && packet.target != null) {
            // 造成實際傷害
            if (packet.target.level() instanceof ServerLevel serverLevel) {
                DamageSource source = ModDamageSources.custom(serverLevel, packet.attacker);

                if (packet.bypassInvul) {
                    packet.target.invulnerableTime = 0;
                }
                packet.target.hurtMarked = true;
                packet.target.hurt(source, (float) result.damage);
            }

            // 應用特殊效果
            DamageHandler.applyEffects(result, packet.target, defStats);

            // 發送傷害數字
            sendDamageNumbers(packet, result);
        }

        return result;
    }

    /**
     * 發送傷害數字
     */
    private static void sendDamageNumbers(DamagePacket packet, DamageResult result) {
        if (packet.target.level() instanceof ServerLevel) {
            // 發送每種類型的傷害數字
            for (var entry : result.damageBreakdown.entrySet()) {
                if (entry.getValue() > 0) {
                    DamageHandler.sendDamageNumber(
                        packet.target,
                        entry.getValue(),
                        entry.getKey(),
                        result.critical
                    );
                }
            }
        }
    }
}

