package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

 /**
 * 屬性面板 GUI（按 C 開啟）
 * 完全以程式碼繪製，不需要材質。
 */
@OnlyIn(Dist.CLIENT)
public class StatsScreen extends Screen {

    // ── 面板尺寸（動態，在 init() 計算）──
    private static final int PANEL_MAX_W = 280;
    private static final int PANEL_MAX_H = 300;
    /** 底部安全距離：熱欄22 + 血量/飢餓10 + AppleSkin10 + 間距6 = 48 */
    private static final int BOTTOM_SAFE = 48;
    private static final int TOP_SAFE    = 8;

    // ── 顏色 ──
    private static final int C_BG_INNER     = 0xD0181C24;
    private static final int C_BORDER       = 0xFF3A4A6B;
    private static final int C_BORDER_GLOW  = 0x554A7FBF;
    private static final int C_HEADER_BG    = 0xCC1A2540;
    private static final int C_SECTION_LINE = 0x883A4A6B;
    private static final int C_TITLE        = 0xFFE8D87A;
    private static final int C_LABEL        = 0xFFAABBCC;
    private static final int C_VALUE        = 0xFFEEEEEE;
    private static final int C_VALUE_ZERO   = 0xFF666677;
    private static final int C_PHYS         = 0xFFFF8040;
    private static final int C_MAGIC        = 0xFF9B6FFF;
    private static final int C_ENERGY       = 0xFF40D8FF;
    private static final int C_SOUL         = 0xFFD070FF;
    private static final int C_SPATIAL      = 0xFF44FFAA;
    private static final int C_DEF_BG       = 0x44000000;
    private static final int C_CRIT         = 0xFFFF6060;
    private static final int C_LEVEL_BAR    = 0xFF4FC3F7;
    private static final int C_LEVEL_MAX    = 0xFFFFD700;

    // ── 字型高度 ──
    private static final int FONT_H = 9;
    private static final int LINE_H = 13;

    private int panelX, panelY;
    private int panelW, panelH; // init() 內計算

    // 拖曳狀態
    private boolean dragging = false;
    private int dragOffX, dragOffY;

    public StatsScreen() {
        super(Component.translatable("gui.mcnb.stats_panel"));
    }

    @Override
    protected void init() {
        int availH = height - TOP_SAFE - BOTTOM_SAFE;
        int availW = Math.min(width - 20, PANEL_MAX_W);
        panelW = availW;
        panelH = Math.min(availH, PANEL_MAX_H);
        panelX = (width  - panelW) / 2;
        panelY = TOP_SAFE + (availH - panelH) / 2;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ══════════════════════════════════════════════════
    //  主渲染
    // ══════════════════════════════════════════════════

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // 半透明黑色遮罩
        g.fill(0, 0, width, height, 0x88000000);

        drawPanel(g);
        super.render(g, mx, my, pt);
    }

    private void drawPanel(GuiGraphics g) {
        int x = panelX, y = panelY, w = panelW, h = panelH;

        // ── 光暈（最外層擴展 2px）──
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, C_BORDER_GLOW);
        // ── 外框 ──
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, C_BORDER);
        // ── 主體背景 ──
        g.fill(x, y, x + w, y + h, C_BG_INNER);

        // ── 標題列 ──
        int headerH = 22;
        g.fill(x, y, x + w, y + headerH, C_HEADER_BG);
        drawBorderLine(g, x, y + headerH, x + w, y + headerH, C_BORDER);

        // 標題文字（置中）
        String title = "⚔ 屬性面板";
        int titleW = font.width(title);
        g.drawString(font, title, x + (w - titleW) / 2, y + 7, C_TITLE, true);

        // 關閉提示
        String hint = "[ESC]";
        g.drawString(font, hint, x + w - font.width(hint) - 6, y + 7, 0xFF666677, false);

        // ── 讀取 Capability ──
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        int[] levelInfo   = { 1, 0, 0, 0 };
        double[] atk      = new double[2];
        double[] combatEx = new double[4];
        float[]  defenseArr = new float[1];   // [0] = defense  ← fix: use array so lambda can capture it
        double[] special  = new double[3];
        float[]  special2 = new float[3];

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(cap -> {
            levelInfo[0] = cap.getLevel();
            levelInfo[1] = (int) Math.min(cap.getExp(), Integer.MAX_VALUE);
            levelInfo[2] = (int) Math.min(cap.getExpToNextLevel(), Integer.MAX_VALUE);
            levelInfo[3] = cap.getMaxLevel();
        });

        player.getCapability(ModCapabilities.COMBAT_DATA).ifPresent(cap -> {
            atk[0] = cap.getPhysicalAttack();
            atk[1] = cap.getMagicAttack();
            combatEx[0] = cap.getCritChance() * 100;
            combatEx[1] = cap.getCritDamage() * 100;
            combatEx[2] = cap.getPenetration();
            combatEx[3] = cap.getEvasion();
            defenseArr[0] = cap.getDefense();   // fix: write into array
            special[0] = cap.getDamageReduction() * 100;
            special[1] = cap.getHealingBonus() * 100;
            special[2] = cap.getRegen();
            special2[0] = cap.getEnergyShield();
            special2[1] = cap.getMaxEnergyShield();
            special2[2] = cap.getSoulIntegrity();
        });

        int cursor = y + headerH + 6;
        int pad    = x + 8;

        // ── 等級區塊 ──
        cursor = drawLevelSection(g, pad, cursor, x + w, levelInfo);

        // ── 分隔線 ──
        cursor = drawDivider(g, x + 4, x + w - 4, cursor, "攻擊", C_PHYS);

        // ── 攻擊（物理 + 魔法）──
        cursor = drawStatRow(g, pad, cursor, w - 16, "⚔ 物理攻擊", fmtD(atk[0]), C_PHYS);
        cursor = drawStatRow(g, pad, cursor, w - 16, "✦ 魔法攻擊", fmtD(atk[1]), C_MAGIC);

        // ── 戰鬥屬性 ──
        cursor = drawDivider(g, x + 4, x + w - 4, cursor, "戰鬥", C_VALUE);
        cursor = drawStatRow(g, pad, cursor, w - 16, "◎ 暴擊率",    fmt1(combatEx[0]) + "%", C_CRIT);
        cursor = drawStatRow(g, pad, cursor, w - 16, "◈ 暴擊傷害",  "+" + fmt1(combatEx[1]) + "%", C_CRIT);
        cursor = drawStatRow(g, pad, cursor, w - 16, "▶ 穿透值",    fmt1(combatEx[2]), C_VALUE);
        cursor = drawStatRow(g, pad, cursor, w - 16, "◁ 閃避值",    fmt1(combatEx[3]), C_VALUE);

        // ── 防禦 ──
        cursor = drawDivider(g, x + 4, x + w - 4, cursor, "防禦", C_ENERGY);
        cursor = drawStatRow(g, pad, cursor, w - 16, "🛡 防禦", fmtF(defenseArr[0]), C_ENERGY);  // fix: use array value
        cursor = drawStatRow(g, pad, cursor, w - 16, "⬡ 減傷", fmt1(special[0]) + "%", C_VALUE);

        // ── 特殊狀態 ──
        cursor = drawDivider(g, x + 4, x + w - 4, cursor, "特殊", C_SOUL);
        cursor = drawBarRow(g, pad, cursor, w - 20,
                "⚡ 能量護盾", special2[0], special2[1], C_ENERGY);
        cursor = drawBarRow(g, pad, cursor, w - 20,
                "☯ 靈魂完整", special2[2], 100f, C_SOUL);
        cursor = drawStatRow(g, pad, cursor, w - 16, "♥ 生命回復", fmt2(special[2]) + "/s", C_SPATIAL);
        drawStatRow   (g, pad, cursor, w - 16, "✚ 治療加成", "+" + fmt1(special[1]) + "%", C_SPATIAL);
    }

    // ══════════════════════════════════════════════════
    //  等級區塊
    // ══════════════════════════════════════════════════

    private int drawLevelSection(GuiGraphics g, int x, int y, int xRight, int[] info) {
        int level = info[0], exp = info[1], expNext = info[2], maxLevel = info[3];
        boolean isMax = level >= maxLevel;

        // 等級文字
        String lvText = "Lv." + level;
        g.drawString(font, lvText, x, y, C_TITLE, true);

        // exp 文字（右對齊）
        String expText = isMax ? "MAX" : (exp + " / " + expNext);
        int etw = font.width(expText);
        g.drawString(font, expText, xRight - etw - 8, y, isMax ? C_LEVEL_MAX : C_LABEL, false);

        y += FONT_H + 3;

        // 經驗條
        int barW = xRight - x - 8;
        float prog = isMax ? 1f : (expNext > 0 ? (float) exp / expNext : 0f);
        g.fill(x, y, x + barW, y + 5, 0x44FFFFFF);
        int fill = (int)(barW * prog);
        if (fill > 0) g.fill(x, y, x + fill, y + 5, isMax ? C_LEVEL_MAX : C_LEVEL_BAR);
        drawBorderLine(g, x, y, x + barW, y + 5, 0x66AAAAAA);

        return y + 5 + 7;
    }

    // ══════════════════════════════════════════════════
    //  單行屬性
    // ══════════════════════════════════════════════════

    private int drawStatRow(GuiGraphics g, int x, int y, int w, String label, String value, int labelColor) {
        g.drawString(font, label, x, y, labelColor, false);
        int vw = font.width(value);
        int valueColor = value.equals("0") || value.equals("0.0") || value.equals("0%") ? C_VALUE_ZERO : C_VALUE;
        g.drawString(font, value, x + w - vw, y, valueColor, false);
        return y + LINE_H;
    }

    // ══════════════════════════════════════════════════
    //  進度條行（能量護盾 / 靈魂完整性）
    // ══════════════════════════════════════════════════

    private int drawBarRow(GuiGraphics g, int x, int y, int w, String label, float cur, float max, int barColor) {
        g.drawString(font, label, x, y, C_LABEL, false);
        String valStr = fmt1(cur) + " / " + fmt1(max);
        int vw = font.width(valStr);
        g.drawString(font, valStr, x + w - vw, y, C_VALUE, false);

        y += FONT_H + 2;
        int barH = 4;
        g.fill(x, y, x + w, y + barH, C_DEF_BG);
        float prog = max > 0 ? Math.min(1f, cur / max) : 0f;
        int fill = (int)(w * prog);
        if (fill > 0) g.fill(x, y, x + fill, y + barH, barColor);
        drawBorderLine(g, x, y, x + w, y + barH, 0x55888888);

        return y + barH + 5;
    }

    // ══════════════════════════════════════════════════
    //  分節標題線
    // ══════════════════════════════════════════════════

    private int drawDivider(GuiGraphics g, int x1, int x2, int y, String label, int labelColor) {
        y += 2;
        int labelW = font.width(label) + 6;
        int mid    = (x1 + x2) / 2;

        // 左線
        g.fill(x1, y + FONT_H / 2, mid - labelW / 2, y + FONT_H / 2 + 1, C_SECTION_LINE);
        // 標籤
        g.drawString(font, label, mid - font.width(label) / 2, y, labelColor, false);
        // 右線
        g.fill(mid + labelW / 2, y + FONT_H / 2, x2, y + FONT_H / 2 + 1, C_SECTION_LINE);

        return y + FONT_H + 4;
    }

    // ══════════════════════════════════════════════════
    //  輔助：畫矩形邊框
    // ══════════════════════════════════════════════════

    private void drawBorderLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1,      y1,      x2,      y1 + 1,  color);
        g.fill(x1,      y2 - 1,  x2,      y2,      color);
        g.fill(x1,      y1,      x1 + 1,  y2,      color);
        g.fill(x2 - 1,  y1,      x2,      y2,      color);
    }

    // ══════════════════════════════════════════════════
    //  格式化工具
    // ══════════════════════════════════════════════════

    private String fmtD(double v) { return v == (long) v ? String.valueOf((long) v) : String.format("%.1f", v); }
    private String fmtF(float v)  { return v == (int) v  ? String.valueOf((int)  v) : String.format("%.1f", v); }
    private String fmt1(double v) { return String.format("%.1f", v); }
    private String fmt2(double v) { return String.format("%.2f", v); }

    // ══════════════════════════════════════════════════
    //  拖曳支援
    // ══════════════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 &&
                mx >= panelX && mx <= panelX + PANEL_MAX_W &&
                my >= panelY && my <= panelY + 22) {
            dragging  = true;
            dragOffX  = (int) mx - panelX;
            dragOffY  = (int) my - panelY;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            panelX = (int) mx - dragOffX;
            panelY = (int) my - dragOffY;
            // 邊界限制：不可超出安全區
            panelX = Math.max(0, Math.min(width  - panelW, panelX));
            panelY = Math.max(TOP_SAFE, Math.min(height - BOTTOM_SAFE - panelH, panelY));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }
}
