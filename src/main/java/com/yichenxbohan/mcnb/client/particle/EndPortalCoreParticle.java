package com.yichenxbohan.mcnb.client.particle;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

/**
 * 完全複製原版 EndPortalBlock.animateTick 的粒子行為。
 *
 * 原版邏輯（反編譯自 EndPortalBlock.class）：
 *   d0 = blockPos.getX() + random.nextDouble()   // X in [blockX, blockX+1]
 *   d1 = blockPos.getY() + 0.8                   // Y fixed at block top
 *   d2 = blockPos.getZ() + random.nextDouble()   // Z in [blockZ, blockZ+1]
 *   level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0, 0, 0)
 */
@OnlyIn(Dist.CLIENT)
public class EndPortalCoreParticle {

    private static final Random RNG = new Random();

    /**
     * 在指定座標生成與原版終界傳送門方塊完全相同的粒子效果。
     *
     * @param level  客戶端世界
     * @param center 方塊中心座標（等同於 blockPos 的浮點表示）
     * @param tick   未使用，保留以維持介面相容
     */
    public static void spawnPortalFace(Level level, Vec3 center, long tick) {
        // 還原 blockPos 的整數座標（floor）
        int bx = (int) Math.floor(center.x);
        int by = (int) Math.floor(center.y);
        int bz = (int) Math.floor(center.z);

        // 與原版 animateTick 完全一致
        double d0 = bx + RNG.nextDouble();   // x in [bx, bx+1]
        double d1 = by + 0.8;                // y fixed +0.8
        double d2 = bz + RNG.nextDouble();   // z in [bz, bz+1]

        level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
    }
}
