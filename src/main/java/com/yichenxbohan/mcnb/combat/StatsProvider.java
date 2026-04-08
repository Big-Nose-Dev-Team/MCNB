package com.yichenxbohan.mcnb.combat;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.combat.capability.ICombatData;
import com.yichenxbohan.mcnb.level.PlayerAttributeType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class StatsProvider {

    private static final UUID AGILITY_SPEED_UUID = UUID.fromString("7bda8d33-2be5-4e52-9e19-6d6eb3e4c401");
    private static final UUID POTENTIAL_COOLDOWN_UUID = UUID.fromString("1ad45862-4e8a-4d2f-8bc8-18d894fbf642");

    private static final double STRENGTH_PHYSICAL_PER_POINT = 2.5;
    private static final double INTELLIGENCE_MAGIC_PER_POINT = 2.5;
    private static final double CONSTITUTION_DEFENSE_PER_POINT = 1.5;
    private static final double AGILITY_SPEED_PER_POINT = 0.005;
    private static final double POTENTIAL_COOLDOWN_PER_POINT = 0.005;
    private static final double POTENTIAL_COOLDOWN_CAP = 0.75;

    /**
     * 從原版屬性讀取基礎數值，寫入 COMBAT_DATA Capability。
     * 必須在服務端呼叫；客戶端透過 StatsSyncPacket 接收同步資料。
     */
    public static void apply(LivingEntity entity) {
        entity.getCapability(ModCapabilities.COMBAT_DATA).ifPresent(cap -> {
            double[] bonuses = new double[5]; // 0=strength, 1=constitution, 2=potential, 3=intelligence, 4=agility

            if (entity instanceof Player player) {
                player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(levelCap -> {
                    bonuses[0] = levelCap.getAttributePoints(PlayerAttributeType.STRENGTH) * STRENGTH_PHYSICAL_PER_POINT;
                    bonuses[1] = levelCap.getAttributePoints(PlayerAttributeType.CONSTITUTION) * CONSTITUTION_DEFENSE_PER_POINT;
                    bonuses[2] = Math.min(POTENTIAL_COOLDOWN_CAP,
                            levelCap.getAttributePoints(PlayerAttributeType.POTENTIAL) * POTENTIAL_COOLDOWN_PER_POINT);
                    bonuses[3] = levelCap.getAttributePoints(PlayerAttributeType.INTELLIGENCE) * INTELLIGENCE_MAGIC_PER_POINT;
                    bonuses[4] = levelCap.getAttributePoints(PlayerAttributeType.AGILITY) * AGILITY_SPEED_PER_POINT;
                });
            }

            // ── 攻擊 ──
            cap.setPhysicalAttack(entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) + bonuses[0]);
            cap.setMagicAttack(bonuses[3]);

            // 非物理類直接等於魔法攻擊力，倍率由技能調整
            double mag = cap.getMagicAttack();
            cap.setEnergyAttack(mag);
            cap.setSoulAttack(mag);
            cap.setChaosAttack(mag);
            cap.setSpatialAttack(mag);
            cap.setTemporalAttack(mag);
            cap.setTrueAttack(mag);

            // ── 防禦 ──
            cap.setDefense((float) (entity.getArmorValue() + bonuses[1]));

            // ── 額外屬性效果 ──
            if (entity instanceof Player player) {
                applyModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), AGILITY_SPEED_UUID,
                        "mcnb.agility_speed", bonuses[4]);
                applyModifier(player.getAttribute(Attributes.ATTACK_SPEED), POTENTIAL_COOLDOWN_UUID,
                        "mcnb.potential_cooldown", bonuses[2]);
            }
        });
    }

    private static void applyModifier(AttributeInstance instance, UUID uuid, String name, double amount) {
        if (instance == null) return;
        var existing = instance.getModifier(uuid);
        if (existing != null) {
            instance.removeModifier(existing);
        }
        if (amount > 0) {
            instance.addPermanentModifier(new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.MULTIPLY_BASE));
        }
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
