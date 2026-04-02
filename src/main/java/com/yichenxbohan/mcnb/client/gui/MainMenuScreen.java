package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 大選單畫面（按 M 開啟）
 * 包含可點擊的子選單按鈕：
 *   - 職業系統：未選職業→進入選擇畫面；已選職業→進入屬性面板
 *   （未來可擴充更多按鈕）
 */
@OnlyIn(Dist.CLIENT)
public class MainMenuScreen extends Screen {

    private static final int PANEL_MAX_W = 260;
    private static final int PANEL_MAX_H = 320;
    /** 底部安全距離：熱欄22 + 血量/飢餓10 + AppleSkin10 + 間距6 = 48 */
    private static final int BOTTOM_SAFE = 48;
    private static final int TOP_SAFE    = 8;

    private static final int BTN_W = 220;
    private static final int BTN_H = 44;

    // 顏色
    private static final int C_BG      = 0xE8101014;
    private static final int C_BORDER  = 0xFF3A4A6B;
    private static final int C_GLOW    = 0x554A7FBF;
    private static final int C_HEADER  = 0xCC1A2540;
    private static final int C_TITLE   = 0xFFE8D87A;
    private static final int C_CLOSE   = 0xFF666677;

    private int panelX, panelY;
    private int panelW, panelH; // init() 內計算

    // 按鈕資料（動態計算）
    private MenuButton[] buttons;

    public MainMenuScreen() {
        super(Component.literal("選單"));
    }

    @Override
    protected void init() {
        int availH = height - TOP_SAFE - BOTTOM_SAFE;
        int availW = Math.min(width - 20, PANEL_MAX_W);
        panelW = availW;
        panelH = Math.min(availH, PANEL_MAX_H);

        panelX = (width  - panelW) / 2;
        panelY = TOP_SAFE + (availH - panelH) / 2;
        buildButtons();
    }

    private void buildButtons() {
        // ── 取得玩家職業 ──
        PlayerClass cls = PlayerClass.NONE;
        var player = Minecraft.getInstance().player;
        if (player != null) {
            cls = player.getCapability(ModCapabilities.PLAYER_CLASS)
                        .map(c -> c.getPlayerClass())
                        .orElse(PlayerClass.NONE);
        }

        final PlayerClass finalCls = cls;

        int startX = panelX + (panelW - BTN_W) / 2;
        int startY = panelY + 50;

        buttons = new MenuButton[]{
            // ── 職業系統按鈕 ──
            new MenuButton(
                startX, startY,
                BTN_W, BTN_H,
                "⚔  職業系統",
                finalCls == PlayerClass.NONE
                    ? "尚未選擇職業，點擊以進入選擇"
                    : finalCls.icon + " " + finalCls.displayName + " — 點擊查看屬性",
                finalCls == PlayerClass.NONE ? 0xFFFFD700 : finalCls.color,
                finalCls == PlayerClass.NONE ? 0xFFAAAAAA : 0xFFCCCCCC,
                () -> {
                    if (finalCls == PlayerClass.NONE) {
                        Minecraft.getInstance().setScreen(new ClassSelectScreen());
                    } else {
                        Minecraft.getInstance().setScreen(new StatsScreen());
                    }
                }
            )
            // 未來可在此繼續添加按鈕
        };
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x88000000);
        drawPanel(g, mx, my);
        super.render(g, mx, my, pt);
    }

    private void drawPanel(GuiGraphics g, int mx, int my) {
        int x = panelX, y = panelY, w = panelW, h = panelH;

        // 外框 + 背景
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, C_GLOW);
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, C_BORDER);
        g.fill(x, y, x + w, y + h, C_BG);

        // 標題列
        g.fill(x, y, x + w, y + 36, C_HEADER);
        g.fill(x, y + 36, x + w, y + 37, C_BORDER);

        g.drawCenteredString(font, "✦ MCNB 選單 ✦", x + w / 2, y + 7, C_TITLE);
        g.drawString(font, "[ESC]", x + w - font.width("[ESC]") - 6, y + 7, C_CLOSE, false);

        // 玩家職業狀態列
        PlayerClass cls = PlayerClass.NONE;
        var player = Minecraft.getInstance().player;
        if (player != null) {
            cls = player.getCapability(ModCapabilities.PLAYER_CLASS)
                        .map(c -> c.getPlayerClass())
                        .orElse(PlayerClass.NONE);
        }
        String statusText = cls == PlayerClass.NONE
                ? "職業：未選擇"
                : "職業：" + cls.icon + " " + cls.displayName;
        int statusColor = cls == PlayerClass.NONE ? 0xFF888899 : cls.color;
        g.drawCenteredString(font, statusText, x + w / 2, y + 22, statusColor);

        // 按鈕
        if (buttons != null) {
            for (MenuButton btn : buttons) {
                btn.render(g, font, mx, my);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && buttons != null) {
            for (MenuButton btn : buttons) {
                if (btn.isHovered((int) mx, (int) my)) {
                    btn.onClick();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    // ══════════════════════════════════════════════════
    //  內部類：選單按鈕
    // ══════════════════════════════════════════════════

    private static class MenuButton {
        final int x, y, w, h;
        final String title;
        final String subtitle;
        final int titleColor;
        final int subtitleColor;
        final Runnable action;

        // 顏色
        private static final int BG_NORMAL = 0xCC1C2030;
        private static final int BG_HOVER  = 0xCC2A3A60;
        private static final int BORDER_NORMAL = 0xFF3A4A6B;

        MenuButton(int x, int y, int w, int h,
                   String title, String subtitle,
                   int titleColor, int subtitleColor,
                   Runnable action) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.title = title; this.subtitle = subtitle;
            this.titleColor = titleColor; this.subtitleColor = subtitleColor;
            this.action = action;
        }

        boolean isHovered(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }

        void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my) {
            boolean hov = isHovered(mx, my);
            int bg = hov ? BG_HOVER : BG_NORMAL;
            g.fill(x, y, x + w, y + h, bg);

            // 邊框（懸停時用 titleColor 加亮）
            int borderColor = hov ? titleColor : BORDER_NORMAL;
            drawBorder(g, x, y, x + w, y + h, borderColor);

            // 左側彩色豎條
            g.fill(x, y, x + 3, y + h, titleColor);

            // 標題
            g.drawString(font, title, x + 12, y + 10, titleColor, true);
            // 副標題
            g.drawString(font, subtitle, x + 12, y + 24, subtitleColor, false);

            // 右箭頭
            g.drawString(font, "›", x + w - 14, y + h / 2 - 4, hov ? titleColor : 0xFF666677, false);
        }

        void onClick() { action.run(); }

        private static void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
            g.fill(x1,     y1,     x2,     y1 + 1, color);
            g.fill(x1,     y2 - 1, x2,     y2,     color);
            g.fill(x1,     y1,     x1 + 1, y2,     color);
            g.fill(x2 - 1, y1,     x2,     y2,     color);
        }
    }
}

