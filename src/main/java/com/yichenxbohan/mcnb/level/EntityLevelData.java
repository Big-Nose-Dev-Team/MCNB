package com.yichenxbohan.mcnb.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;

/**
 * 怪物等級資料
 * 等級根據怪物最大血量 + 所在維度自動推算
 *
 * 維度等級範圍：
 *   主世界 (overworld)  → 1  ~ 10
 *   地獄   (the_nether) → 10 ~ 20
 *   終界   (the_end)    → 20 ~ 30
 *   其他維度            → 1  ~ 30（不限制）
 */
public class EntityLevelData implements IEntityLevel {

    public static final int MAX_LEVEL = 100;

    private int level = 1;

    // ==================== 等級推算 ====================

    /**
     * 根據生物最大血量推算 0~1 的相對強度值（0 = 最弱，1 = 最強）
     */
    private static double inferStrength(LivingEntity entity) {
        float hp = entity.getMaxHealth();
        // 用 sigmoid-like 曲線把血量壓到 0~1，參考值：hp=20 ≈ 0.25, hp=200 ≈ 0.85
        return 1.0 - 1.0 / (1.0 + Math.pow(hp / 30.0, 0.7));
    }

    /**
     * 根據所在維度決定等級範圍，再用強度映射到該範圍內
     *
     * Boss（龍 / 凋零）保持固定高等級，不受維度範圍影響
     */
    public static int inferLevel(LivingEntity entity, ResourceKey<Level> dimension) {
        // Boss 固定等級
        if (entity instanceof EnderDragon) return 100;
        if (entity instanceof WitherBoss)  return 95;

        // 根據維度決定等級範圍
        int minLv, maxLv;
        if (dimension == Level.OVERWORLD) {
            minLv = 1;  maxLv = 10;
        } else if (dimension == Level.NETHER) {
            minLv = 10; maxLv = 20;
        } else if (dimension == Level.END) {
            minLv = 20; maxLv = 30;
        } else {
            // 其他模組維度不限制
            minLv = 1;  maxLv = 30;
        }

        double strength = inferStrength(entity);
        int base = minLv + (int) Math.round(strength * (maxLv - minLv));

        // ±1 的小偏差（偽隨機，同一實體穩定）
        int jitter = (int)((entity.getId() % 3) - 1);

        return Math.max(minLv, Math.min(maxLv, base + jitter));
    }

    // ==================== IEntityLevel ====================

    @Override
    public int getLevel() { return level; }

    @Override
    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, level));
    }

    // ==================== NBT ====================

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("entity_level", level);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("entity_level")) {
            this.level = Math.max(1, Math.min(MAX_LEVEL, tag.getInt("entity_level")));
        }
    }
}
