package com.yichenxbohan.mcnb.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 終界傳送門核心粒子驅動器（客戶端）
 *
 * 負責：
 *  1. 維護一組「活躍粒子核心」的世界座標清單
 *  2. 每個客戶端 tick 呼叫 EndPortalCoreParticle.spawnAll() 驅動所有核心
 *
 * 使用方式：
 *   // 在任何客戶端程式碼中新增一個核心：
 *   EndPortalParticleHandler.addCore(new Vec3(x, y, z));
 *
 *   // 移除核心：
 *   EndPortalParticleHandler.removeCore(vec3);
 *
 *   // 清除全部核心（例如切換世界時）：
 *   EndPortalParticleHandler.clearCores();
 */
@OnlyIn(Dist.CLIENT)
public class EndPortalParticleHandler {

    /** 目前活躍的粒子核心座標列表 */
    private static final List<Vec3> activeCores = new ArrayList<>();

    /** 全局 tick 計數，驅動旋轉相位 */
    private static long globalTick = 0L;

    // ─────────────────────────────────────────────────────────
    // 公開 API
    // ─────────────────────────────────────────────────────────

    /**
     * 在指定世界座標新增一個終界核心粒子發射點。
     * 同一座標不會重複新增。
     */
    public static void addCore(Vec3 position) {
        for (Vec3 existing : activeCores) {
            if (existing.distanceToSqr(position) < 0.01) return; // 去重
        }
        activeCores.add(position);
    }

    /**
     * 移除距離 {@code position} 最近（0.1 格內）的核心。
     */
    public static void removeCore(Vec3 position) {
        activeCores.removeIf(v -> v.distanceToSqr(position) < 0.01);
    }

    /**
     * 清除所有活躍核心（切換維度 / 世界時呼叫）。
     */
    public static void clearCores() {
        activeCores.clear();
    }

    /** 取得目前活躍核心數量（除錯用）。 */
    public static int getCoreCount() {
        return activeCores.size();
    }

    // ─────────────────────────────────────────────────────────
    // 每 tick 驅動
    // ─────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // 只在 END 階段執行一次，避免每個 phase 重複
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        // 世界不存在、暫停或無玩家時跳過
        if (level == null || mc.player == null || mc.isPaused()) return;

        globalTick++;

        // 若無活躍核心則提早返回
        if (activeCores.isEmpty()) return;

        // 取得玩家位置，用於視距裁剪（超過 32 格不渲染）
        Vec3 playerPos = mc.player.position();
        final double MAX_DIST_SQ = 32.0 * 32.0;

        for (Vec3 core : activeCores) {
            if (playerPos.distanceToSqr(core) > MAX_DIST_SQ) continue;
            EndPortalCoreParticle.spawnPortalFace(level, core, globalTick);
        }
    }
}
