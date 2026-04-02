package com.yichenxbohan.mcnb.client.level;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客戶端升級通知管理器
 */
@OnlyIn(Dist.CLIENT)
public class LevelUpNotifier {

    private static int displayLevel = 0;
    private static int ticksRemaining = 0;

    // 顯示持續時間（tick，100 = 5 秒）
    private static final int DISPLAY_DURATION = 100;

    public static void showLevelUp(int newLevel) {
        displayLevel = newLevel;
        ticksRemaining = DISPLAY_DURATION;
    }

    public static void tick() {
        if (ticksRemaining > 0) ticksRemaining--;
    }

    public static boolean isDisplaying() {
        return ticksRemaining > 0;
    }

    public static int getDisplayLevel() {
        return displayLevel;
    }

    /** 0.0 ~ 1.0 的透明度，用於淡出 */
    public static float getAlpha() {
        if (ticksRemaining <= 0) return 0f;
        if (ticksRemaining > 20) return 1.0f;
        return ticksRemaining / 20.0f;
    }
}

