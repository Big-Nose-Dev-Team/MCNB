package com.yichenxbohan.mcnb.client.level;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.level.IPlayerLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 玩家等級 HUD 渲染器
 * 在畫面右下角顯示等級與經驗條（透明背景）
 */
@OnlyIn(Dist.CLIENT)
public class PlayerLevelHudRenderer {

    // ==================== 尺寸常數 ====================
    private static final int BAR_WIDTH  = 100;
    private static final int BAR_HEIGHT = 5;
    private static final int PADDING    = 3;
    private static final int MARGIN_X   = 10;
    /**
     * 距離畫面底部的偏移：
     *   22  = 熱欄高度
     *   10  = 原版血量/飢餓列高度
     *   10  = AppleSkin 飽食度視覺列
     *   10  = 額外安全間距
     *   合計 = 52，避免與 AppleSkin 等 HUD 模組重疊
     */
    private static final int MARGIN_Y   = 52;

    // ==================== 顏色 ====================
    private static final int COLOR_BAR_BG   = 0x55000000; // 半透明條背景
    private static final int COLOR_BAR_FILL = 0xFF4FC3F7; // 淡藍色進度
    private static final int COLOR_BAR_MAX  = 0xFFFFD700; // 滿等金色
    private static final int COLOR_LEVEL    = 0xFFFFFFFF;
    private static final int COLOR_EXP_TEXT = 0xFFAAAAAA;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        mc.player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(cap -> {
            GuiGraphics gui = event.getGuiGraphics();
            int screenW = mc.getWindow().getGuiScaledWidth();
            int screenH = mc.getWindow().getGuiScaledHeight();
            renderLevelBar(gui, mc, cap, screenW, screenH);
        });
    }

    // ==================== 等級條渲染 ====================

    private static void renderLevelBar(GuiGraphics gui, Minecraft mc, IPlayerLevel cap,
                                        int screenW, int screenH) {
        boolean isMax = cap.isMaxLevel();
        float progress = cap.getLevelProgress();

        String levelText = "Lv." + cap.getLevel();
        String expText = isMax ? "MAX" : (cap.getExp() + " / " + cap.getExpToNextLevel());

        int fontH  = mc.font.lineHeight;
        int totalH = fontH + PADDING + BAR_HEIGHT;
        int startX = screenW - MARGIN_X - BAR_WIDTH;
        int startY = screenH - MARGIN_Y - totalH;

        // 不畫背景板 — 完全透明

        // 等級文字（左側，帶陰影）
        gui.drawString(mc.font, levelText, startX, startY, COLOR_LEVEL, true);

        // EXP 文字（右側對齊，帶陰影）
        int expTextW = mc.font.width(expText);
        gui.drawString(mc.font, expText,
                startX + BAR_WIDTH - expTextW, startY,
                COLOR_EXP_TEXT, true);

        // 經驗條背景（淺色半透明軌道）
        int barY = startY + fontH + PADDING;
        gui.fill(startX, barY, startX + BAR_WIDTH, barY + BAR_HEIGHT, COLOR_BAR_BG);

        // 經驗條填充
        int fillColor = isMax ? COLOR_BAR_MAX : COLOR_BAR_FILL;
        int fillW = (int)(BAR_WIDTH * progress);
        if (fillW > 0) {
            gui.fill(startX, barY, startX + fillW, barY + BAR_HEIGHT, fillColor);
        }

        // 細邊框
        drawBorder(gui, startX, barY, BAR_WIDTH, BAR_HEIGHT, 0x88888888);
    }

    // ==================== 工具 ====================

    private static void drawBorder(GuiGraphics gui, int x, int y, int w, int h, int color) {
        gui.fill(x,         y,         x + w,     y + 1,     color); // 上
        gui.fill(x,         y + h - 1, x + w,     y + h,     color); // 下
        gui.fill(x,         y,         x + 1,     y + h,     color); // 左
        gui.fill(x + w - 1, y,         x + w,     y + h,     color); // 右
    }
}
