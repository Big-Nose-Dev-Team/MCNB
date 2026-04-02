package com.yichenxbohan.mcnb.level;

import com.yichenxbohan.mcnb.ModCapabilities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 等級差距傷害修正工具類
 *
 * 規則：
 *   - 攻擊方每比防禦方高 1 等：攻擊方傷害 +5%
 *   - 攻擊方每比防禦方低 1 等：攻擊方傷害 -5%
 *   - 攻擊方等級低於防禦方 20 等（含）以上：傷害歸零（無法造成傷害）
 *   - 係數最低 0，最高不設上限（高等級欺負低等級可造成巨量傷害）
 */
public class LevelDamageModifier {

    /** 每等級差距的傷害倍率變化 */
    public static final float PER_LEVEL_BONUS = 0.05f;

    /** 低於目標超過此等差時傷害歸零 */
    public static final int LEVEL_GAP_IMMUNE = 20;

    /**
     * 取得攻擊方對防禦方的傷害倍率
     * @return 0.0 代表完全無法傷害，1.0 = 無加成/減益
     */
    public static float getMultiplier(LivingEntity attacker, LivingEntity target) {
        int atkLevel = getLevel(attacker);
        int defLevel = getLevel(target);
        return getMultiplier(atkLevel, defLevel);
    }

    public static float getMultiplier(int atkLevel, int defLevel) {
        int diff = atkLevel - defLevel; // 正 = 攻擊方較高

        // 低於 20 等以上 → 無法傷害
        if (diff <= -LEVEL_GAP_IMMUNE) return 0f;

        // 每等差 ±5%
        float multiplier = 1.0f + diff * PER_LEVEL_BONUS;
        return Math.max(0f, multiplier);
    }

    /**
     * 取得實體的等級
     * 玩家用 PLAYER_LEVEL Capability，怪物用 ENTITY_LEVEL Capability
     */
    public static int getLevel(LivingEntity entity) {
        if (entity instanceof Player) {
            return entity.getCapability(ModCapabilities.PLAYER_LEVEL)
                    .map(IPlayerLevel::getLevel)
                    .orElse(1);
        } else {
            return entity.getCapability(ModCapabilities.ENTITY_LEVEL)
                    .map(IEntityLevel::getLevel)
                    .orElse(1);
        }
    }
}

