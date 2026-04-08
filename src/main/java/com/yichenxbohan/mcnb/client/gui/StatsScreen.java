package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.level.PlayerAttributeType;
import com.yichenxbohan.mcnb.network.AttributePointPacket;
import com.yichenxbohan.mcnb.network.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 屬性點分配面板。
 */
@OnlyIn(Dist.CLIENT)
public class StatsScreen extends Screen {

    private static final int PANEL_MAX_W = 460;
    private static final int PANEL_MAX_H = 420;
    private static final int PANEL_MIN_W = 260;
    private static final int PANEL_MIN_H = 220;
    private static final int BOTTOM_SAFE = 48;
    private static final int TOP_SAFE = 8;

    private static final int C_BG_INNER = 0xD0181C24;
    private static final int C_BORDER = 0xFF3A4A6B;
    private static final int C_BORDER_GLOW = 0x554A7FBF;
    private static final int C_HEADER_BG = 0xCC1A2540;
    private static final int C_TITLE = 0xFFE8D87A;
    private static final int C_LABEL = 0xFFAABBCC;
    private static final int C_VALUE = 0xFFEEEEEE;
    private static final int C_VALUE_ZERO = 0xFF666677;
    private static final int C_LEVEL_BAR = 0xFF4FC3F7;
    private static final int C_LEVEL_MAX = 0xFFFFD700;
    private static final int C_ROW_BG = 0xAA121821;
    private static final int C_ROW_BG_ALT = 0xAA171D28;
    private static final int C_BTN_BG = 0xCC1C2030;
    private static final int C_BTN_BG_HOVER = 0xCC2A3A60;
    private static final int C_BTN_BG_DISABLED = 0xCC232323;

    private static final int HEADER_H = 22;
    private int panelX, panelY, panelW, panelH;
    private int contentScroll = 0;
    private int maxScroll = 0;
    private boolean dragging = false;
    private int dragOffX, dragOffY;

    private final int[] attributePoints = new int[PlayerAttributeType.values().length];
    private final Rect[] minusRects = new Rect[PlayerAttributeType.values().length];
    private final Rect[] plusRects = new Rect[PlayerAttributeType.values().length];
    private final Rect resetRect = new Rect();

    private int level = 1;
    private int exp = 0;
    private int expNext = 0;
    private int maxLevel = 100;
    private int allocatedPoints = 0;
    private int availablePoints = 0;

    private double physicalAttack = 0;
    private double magicAttack = 0;
    private double critChance = 0;
    private double critDamage = 0;
    private double penetration = 0;
    private double evasion = 0;
    private float defense = 0;
    private double damageReduction = 0;
    private float energyShield = 0;
    private float maxEnergyShield = 0;
    private float soulIntegrity = 0;
    private double regen = 0;
    private double healingBonus = 0;

    public StatsScreen() {
        super(Component.literal("屬性分配"));
        for (int i = 0; i < attributePoints.length; i++) {
            minusRects[i] = new Rect();
            plusRects[i] = new Rect();
        }
    }

    @Override
    protected void init() {
        int availH = height - TOP_SAFE - BOTTOM_SAFE;
        panelW = Mth.clamp((int) (width * 0.54f), PANEL_MIN_W, PANEL_MAX_W);
        panelW = Math.min(panelW, Math.max(160, width - 12));
        panelH = Mth.clamp((int) (height * 0.74f), PANEL_MIN_H, PANEL_MAX_H);
        panelH = Math.min(panelH, Math.max(160, availH));

        panelX = (width - panelW) / 2;
        panelY = TOP_SAFE + (availH - panelH) / 2;

        contentScroll = 0;
        maxScroll = 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x88000000);
        captureData();
        drawPanel(g, mx, my);
        super.render(g, mx, my, pt);
    }

    private void captureData() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(cap -> {
            level = cap.getLevel();
            exp = (int) Math.min(cap.getExp(), Integer.MAX_VALUE);
            expNext = (int) Math.min(cap.getExpToNextLevel(), Integer.MAX_VALUE);
            maxLevel = cap.getMaxLevel();
            allocatedPoints = cap.getAllocatedAttributePoints();
            availablePoints = cap.getAvailableAttributePoints();
            for (PlayerAttributeType type : PlayerAttributeType.orderedValues()) {
                attributePoints[type.ordinal()] = cap.getAttributePoints(type);
            }
        });

        player.getCapability(ModCapabilities.COMBAT_DATA).ifPresent(cap -> {
            physicalAttack = cap.getPhysicalAttack();
            magicAttack = cap.getMagicAttack();
            critChance = cap.getCritChance() * 100.0;
            critDamage = cap.getCritDamage() * 100.0;
            penetration = cap.getPenetration();
            evasion = cap.getEvasion();
            defense = cap.getDefense();
            damageReduction = cap.getDamageReduction() * 100.0;
            energyShield = cap.getEnergyShield();
            maxEnergyShield = cap.getMaxEnergyShield();
            soulIntegrity = cap.getSoulIntegrity();
            regen = cap.getRegen();
            healingBonus = cap.getHealingBonus() * 100.0;
        });
    }

    private void drawPanel(GuiGraphics g, int mx, int my) {
        int x = panelX, y = panelY, w = panelW, h = panelH;

        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, C_BORDER_GLOW);
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, C_BORDER);
        g.fill(x, y, x + w, y + h, C_BG_INNER);

        g.fill(x, y, x + w, y + HEADER_H, C_HEADER_BG);
        drawBorderLine(g, x, y + HEADER_H, x + w, y + HEADER_H, C_BORDER);
        g.drawCenteredString(font, "✦ 屬性分配 ✦", x + w / 2, y + 7, C_TITLE);
        g.drawString(font, "[ESC]", x + w - font.width("[ESC]") - 6, y + 7, 0xFF666677, false);

        var player = Minecraft.getInstance().player;
        if (player == null) {
            g.drawCenteredString(font, "請先進入世界", x + w / 2, y + h / 2, C_LABEL);
            return;
        }

        int contentTop = y + HEADER_H + 4;
        int contentBottom = y + h - 6;
        int contentH = Math.max(0, contentBottom - contentTop);
        int cursor = contentTop + 2 - contentScroll;

        g.enableScissor(x + 2, contentTop, x + w - 2, contentBottom);
        cursor = drawLevelSection(g, x + 8, cursor, w - 16);
        cursor = drawCombatDisplay(g, x + 8, cursor, w - 16);
        cursor = drawPointSummary(g, x + 8, cursor, w - 16, mx, my);
        for (PlayerAttributeType type : PlayerAttributeType.orderedValues()) {
            cursor = drawAttributeRow(g, x + 8, cursor, w - 16, type, mx, my);
        }
        g.disableScissor();

        int totalContentHeight = (cursor + contentScroll) - (contentTop + 2);
        maxScroll = Math.max(0, totalContentHeight - contentH);
        contentScroll = Mth.clamp(contentScroll, 0, maxScroll);
        drawScrollBar(g, x + w - 5, contentTop, contentBottom);
    }

    private int drawLevelSection(GuiGraphics g, int x, int y, int w) {
        int rowH = 34;
        g.fill(x, y, x + w, y + rowH, C_ROW_BG);
        drawBorderLine(g, x, y, x + w, y + rowH, 0x557788AA);

        boolean maxed = level >= maxLevel;
        g.drawString(font, "Lv." + level, x + 8, y + 5, C_TITLE, false);

        String expText = maxed ? "MAX" : (exp + " / " + expNext);
        int expW = font.width(expText);
        g.drawString(font, expText, x + w - expW - 8, y + 5, maxed ? C_LEVEL_MAX : C_LABEL, false);

        int barY = y + 18;
        int barW = w - 16;
        float prog = maxed ? 1f : (expNext > 0 ? (float) exp / expNext : 0f);
        g.fill(x + 8, barY, x + 8 + barW, barY + 5, 0x44FFFFFF);
        int fill = (int) (barW * prog);
        if (fill > 0) {
            g.fill(x + 8, barY, x + 8 + fill, barY + 5, maxed ? C_LEVEL_MAX : C_LEVEL_BAR);
        }
        drawBorderLine(g, x + 8, barY, x + 8 + barW, barY + 5, 0x66AAAAAA);

        return y + rowH + 6;
    }

    private int drawPointSummary(GuiGraphics g, int x, int y, int w, int mx, int my) {
        int rowH = 32;
        g.fill(x, y, x + w, y + rowH, C_ROW_BG_ALT);
        drawBorderLine(g, x, y, x + w, y + rowH, 0x557788AA);

        String left = "剩餘點數：" + availablePoints;
        g.drawString(font, left, x + 8, y + 6, availablePoints > 0 ? C_LEVEL_BAR : C_VALUE_ZERO, false);
        g.drawString(font, "已分配：" + allocatedPoints, x + 8, y + 18, C_LABEL, false);

        int btnW = 76;
        int btnH = 18;
        int bx = x + w - btnW - 8;
        int by = y + 7;
        resetRect.set(bx, by, btnW, btnH);
        boolean enabled = allocatedPoints > 0;
        drawButton(g, resetRect, "重置", enabled, mx, my, 0xFFFFD35A, 0xFF7F6A2C, 0xFF4D4D4D);

        return y + rowH + 6;
    }

    private int drawCombatDisplay(GuiGraphics g, int x, int y, int w) {
        y = drawSectionHeader(g, x, y, w, "攻擊");
        y = drawValueRow(g, x, y, w, "物理攻擊", fmt1(physicalAttack));
        y = drawValueRow(g, x, y, w, "魔法攻擊", fmt1(magicAttack));

        y = drawSectionHeader(g, x, y, w, "戰鬥");
        y = drawValueRow(g, x, y, w, "暴擊率", fmt1(critChance) + "%");
        y = drawValueRow(g, x, y, w, "暴擊傷害", "+" + fmt1(critDamage) + "%");
        y = drawValueRow(g, x, y, w, "穿透值", fmt1(penetration));
        y = drawValueRow(g, x, y, w, "閃避值", fmt1(evasion));

        y = drawSectionHeader(g, x, y, w, "防禦");
        y = drawValueRow(g, x, y, w, "防禦", fmt1(defense));
        y = drawValueRow(g, x, y, w, "減傷", fmt1(damageReduction) + "%");

        y = drawSectionHeader(g, x, y, w, "特殊");
        y = drawValueRow(g, x, y, w, "能量護盾", fmt1(energyShield) + " / " + fmt1(maxEnergyShield));
        y = drawValueRow(g, x, y, w, "靈魂完整", fmt1(soulIntegrity) + "%");
        y = drawValueRow(g, x, y, w, "生命回復", fmt2(regen) + "/s");
        y = drawValueRow(g, x, y, w, "治療加成", "+" + fmt1(healingBonus) + "%");
        return y;
    }

    private int drawSectionHeader(GuiGraphics g, int x, int y, int w, String label) {
        int h = 16;
        g.fill(x, y, x + w, y + h, 0xAA1A2538);
        drawBorderLine(g, x, y, x + w, y + h, 0x557788AA);
        g.drawString(font, label, x + 6, y + 4, C_LABEL, false);
        return y + h;
    }

    private int drawValueRow(GuiGraphics g, int x, int y, int w, String label, String value) {
        int h = 14;
        g.fill(x, y, x + w, y + h, C_ROW_BG_ALT);
        g.drawString(font, label, x + 6, y + 3, C_LABEL, false);
        int valueW = font.width(value);
        int valueColor = (value.equals("0") || value.equals("0.0") || value.equals("0.0%")) ? C_VALUE_ZERO : C_VALUE;
        g.drawString(font, value, x + w - valueW - 6, y + 3, valueColor, false);
        return y + h;
    }

    private int drawAttributeRow(GuiGraphics g, int x, int y, int w, PlayerAttributeType type, int mx, int my) {
        int idx = type.ordinal();
        int rowH = 46;
        boolean alt = (idx & 1) == 1;
        g.fill(x, y, x + w, y + rowH, alt ? C_ROW_BG_ALT : C_ROW_BG);
        drawBorderLine(g, x, y, x + w, y + rowH, 0x447788AA);

        int pts = attributePoints[idx];
        String bonus = type.formatBonus(pts);
        String pointsText = "點數：" + pts;
        String desc = type.getDescription();

        g.drawString(font, type.getLabel(), x + 8, y + 6, type.getColor(), false);
        g.drawString(font, pointsText, x + 8, y + 18, C_VALUE, false);
        g.drawString(font, bonus, x + 8, y + 30, type.getColor(), false);

        int bonusW = font.width(bonus);
        int descW = font.width(desc);
        g.drawString(font, desc, x + w - Math.max(descW, bonusW) - 8, y + 6, C_LABEL, false);

        int btnSize = 16;
        int btnY = y + 15;
        Rect minus = minusRects[idx];
        Rect plus = plusRects[idx];
        minus.set(x + w - 40, btnY, btnSize, btnSize);
        plus.set(x + w - 20, btnY, btnSize, btnSize);

        drawButton(g, minus, "−", pts > 0, mx, my, type.getColor(), 0xFF354155, C_BTN_BG_DISABLED);
        drawButton(g, plus, "+", availablePoints > 0, mx, my, type.getColor(), 0xFF354155, C_BTN_BG_DISABLED);

        return y + rowH + 6;
    }

    private void drawScrollBar(GuiGraphics g, int x, int top, int bottom) {
        if (maxScroll <= 0) return;

        int trackW = 3;
        int trackH = bottom - top;
        if (trackH <= 0) return;

        g.fill(x, top, x + trackW, bottom, 0x442A3344);
        float ratio = (float) trackH / (trackH + maxScroll);
        int thumbH = Math.max(16, (int) (trackH * ratio));
        int travel = trackH - thumbH;
        int thumbY = top + (maxScroll == 0 ? 0 : (int) (travel * (contentScroll / (float) maxScroll)));
        g.fill(x, thumbY, x + trackW, thumbY + thumbH, 0xAA9BB6FF);
    }

    private void drawButton(GuiGraphics g, Rect rect, String label, boolean enabled, int mx, int my,
                            int activeColor, int hoverBorder, int disabledBg) {
        boolean hovered = rect.contains(mx, my);
        int bg = enabled ? (hovered ? C_BTN_BG_HOVER : C_BTN_BG) : disabledBg;
        int border = enabled ? (hovered ? activeColor : hoverBorder) : 0xFF444455;
        int text = enabled ? (hovered ? activeColor : 0xFFEEEEEE) : 0xFF666677;
        g.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, bg);
        drawBorderLine(g, rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, border);
        g.drawCenteredString(font, label, rect.x + rect.w / 2, rect.y + 5, text);
    }

    private void drawBorderLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private String fmt1(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private String fmt2(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);

        if (dragHandleHit(mx, my)) {
            dragging = true;
            dragOffX = (int) mx - panelX;
            dragOffY = (int) my - panelY;
            return true;
        }

        if (resetRect.contains((int) mx, (int) my) && allocatedPoints > 0) {
            ModNetworking.sendToServer(new AttributePointPacket(true));
            return true;
        }

        for (PlayerAttributeType type : PlayerAttributeType.orderedValues()) {
            int idx = type.ordinal();
            if (plusRects[idx].contains((int) mx, (int) my) && availablePoints > 0) {
                ModNetworking.sendToServer(new AttributePointPacket(type, 1));
                return true;
            }
            if (minusRects[idx].contains((int) mx, (int) my) && attributePoints[idx] > 0) {
                ModNetworking.sendToServer(new AttributePointPacket(type, -1));
                return true;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    private boolean dragHandleHit(double mx, double my) {
        return mx >= panelX && mx <= panelX + panelW && my >= panelY && my <= panelY + HEADER_H;
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
            panelX = Math.max(0, Math.min(width - panelW, panelX));
            panelY = Math.max(TOP_SAFE, Math.min(height - BOTTOM_SAFE - panelH, panelY));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        boolean inPanel = mx >= panelX && mx <= panelX + panelW && my >= panelY && my <= panelY + panelH;
        if (inPanel && maxScroll > 0) {
            int step = 14;
            contentScroll = Mth.clamp(contentScroll - (int) (delta * step), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    private static final class Rect {
        int x, y, w, h;

        void set(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean contains(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }
}
