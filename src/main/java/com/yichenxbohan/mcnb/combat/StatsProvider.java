package com.yichenxbohan.mcnb.combat;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.combat.capability.ICombatData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StatsProvider {

    /**
     * 從原版屬性讀取基礎數值，寫入 COMBAT_DATA Capability。
     * 必須在服務端呼叫；客戶端透過 StatsSyncPacket 接收同步資料。
     */
    public static void apply(LivingEntity entity) {
        entity.getCapability(ModCapabilities.COMBAT_DATA).ifPresent(cap -> {
            // ── 攻擊 ──
            cap.setPhysicalAttack(entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            // 魔法攻擊力由裝備/技能設定，此處保留原值（預設 0）

            // 非物理類直接等於魔法攻擊力，倍率由技能調整
            double mag = cap.getMagicAttack();
            cap.setEnergyAttack(mag);
            cap.setSoulAttack(mag);
            cap.setChaosAttack(mag);
            cap.setSpatialAttack(mag);
            cap.setTemporalAttack(mag);
            cap.setTrueAttack(mag);

            // ── 防禦 ──
            cap.setDefense((float) entity.getArmorValue());
        });
    }

    /**
     * 取得 Capability 中的 CombatStats 快照（供戰鬥計算使用）。
     */
    public static CombatStats get(LivingEntity entity) {
        return entity.getCapability(ModCapabilities.COMBAT_DATA)
                .map(ICombatData::getStats)
                .orElseGet(() -> {
                    // Fallback：沒有 Capability 時從原版屬性建構
                    CombatStats s = new CombatStats();
                    s.physicalAttack = entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                    s.defense        = entity.getArmorValue();
                    s.penetration    = 30;
                    s.critChance     = 0.2;
                    s.critDamage     = 0.5;
                    s.damageBonus    = 1.0;
                    s.damageReduction = 0.1;
                    s.evasion        = 10;
                    return s;
                });
    }
}
