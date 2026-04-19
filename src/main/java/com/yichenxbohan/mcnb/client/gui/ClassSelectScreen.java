package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.network.ClassSelectPacket;
import com.yichenxbohan.mcnb.network.ModNetworking;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClassSelectScreen extends Screen {

    private static final ResourceLocation UI_TEX = new ResourceLocation("mcnb", "textures/gui/skill_ui.png");
    private static final int UI_TEX_W = 64;
    private static final int UI_TEX_H = 64;

    private static final int PANEL_MAX_W = 640;
    private static final int PANEL_MAX_H = 440;
    private static final int DESIGN_W = 640;
    private static final int DESIGN_H = 440;
    private static final int TOP_SAFE = 8;
    private static final int BOTTOM_SAFE = 48;

    private static final int C_BG = 0xE9101012;
    private static final int C_BORDER = 0xFF6A6A6A;
    private static final int C_HEADER_BG = 0xFF252525;
    private static final int C_TEXT = 0xFFECECEC;
    private static final int C_WARN = 0xFFFF8A8A;

    private static final PlayerClass[] CLASSES = {
        PlayerClass.SWORDSMAN,
        PlayerClass.MAGE,
        PlayerClass.ARCHER,
        PlayerClass.CULTIVATOR,
        PlayerClass.SUMMONER,
        PlayerClass.WHITE_MAGE,
        PlayerClass.ASSASSIN
    };

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private float layoutScale = 1.0f;
    private int layoutX;
    private int layoutY;
    private int layoutW;
    private int layoutH;

    private Rect prevRect;
    private Rect nextRect;
    private Rect confirmRect;
    private Rect backRect;
    private final List<Rect> iconRects = new ArrayList<>();

    private int currentIndex = 0;
    private PlayerClass selectedClass = CLASSES[0];

    public ClassSelectScreen() {
        super(Component.literal("選擇職業"));
    }

    @Override
    protected void init() {
        int availH = height - TOP_SAFE - BOTTOM_SAFE;
        panelW = Math.min(width - 24, PANEL_MAX_W);
        panelH = Math.min(availH, PANEL_MAX_H);
        panelX = (width - panelW) / 2;
        panelY = TOP_SAFE + (availH - panelH) / 2;

        // 以固定設計稿尺寸做縮放，避免不同螢幕比例下元件彼此漂移。
        layoutScale = Math.min(panelW / (float) DESIGN_W, panelH / (float) DESIGN_H);
        layoutW = Math.round(DESIGN_W * layoutScale);
        layoutH = Math.round(DESIGN_H * layoutScale);
        layoutX = panelX + (panelW - layoutW) / 2;
        layoutY = panelY + (panelH - layoutH) / 2;

        int navY = uy(48);
        prevRect = new Rect(ux(190), navY, us(58), us(22));
        nextRect = new Rect(ux(392), navY, us(58), us(22));

        int bottomY = uy(406);
        confirmRect = new Rect(ux(210), bottomY, us(220), us(22));
        backRect = new Rect(ux(12), bottomY, us(76), us(22));

        rebuildIconRects();
    }

    private void rebuildIconRects() {
        iconRects.clear();
        int iconSize = us(38);
        int gap = us(8);
        int totalW = CLASSES.length * iconSize + (CLASSES.length - 1) * gap;
        int startX = layoutX + (layoutW - totalW) / 2;
        int y = uy(98);
        for (int i = 0; i < CLASSES.length; i++) {
            iconRects.add(new Rect(startX + i * (iconSize + gap), y, iconSize, iconSize));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x90000000);
        drawFrame(g);
        drawHeader(g);
        drawTopSelector(g, mx, my);
        drawClassIconStrip(g, mx, my);
        drawStatsArea(g);
        drawFooter(g, mx, my);
        super.render(g, mx, my, pt);
    }

    private void drawFrame(GuiGraphics g) {
        drawPanelBox(g, panelX, panelY, panelW, panelH);
        g.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, C_BG & 0xAAFFFFFF);
    }

    private void drawHeader(GuiGraphics g) {
        PlayerClass cls = CLASSES[currentIndex];
        int titleW = us(180);
        int titleH = us(24);
        int titleX = ux((DESIGN_W - 180) / 2);
        int titleY = uy(12);
        drawPanelBox(g, titleX - 2, titleY - 2, titleW + 4, titleH + 4);
        g.fill(titleX, titleY, titleX + titleW, titleY + titleH, C_HEADER_BG);
        g.drawCenteredString(font, cls.displayName, titleX + titleW / 2, titleY + us(8), C_TEXT);
    }

    private void drawTopSelector(GuiGraphics g, int mx, int my) {
        PlayerClass cls = CLASSES[currentIndex];
        drawSmallButton(g, prevRect, "<<", mx, my);
        drawSmallButton(g, nextRect, ">>", mx, my);

        int previewW = us(84);
        int previewH = us(84);
        int px = ux((DESIGN_W - 84) / 2);
        int py = uy(42);
        drawPanelBox(g, px, py, previewW, previewH);
        g.fill(px + us(3), py + us(3), px + previewW - us(3), py + previewH - us(3), 0x552A2A40);
        int midSlot = us(34);
        drawSlot(g, new Rect(px + (previewW - midSlot) / 2, py + (previewH - midSlot) / 2, midSlot, midSlot), true);
        g.drawCenteredString(font, cls.icon, px + previewW / 2, py + us(35), cls.color);
    }

    private void drawClassIconStrip(GuiGraphics g, int mx, int my) {
        for (int i = 0; i < CLASSES.length; i++) {
            PlayerClass cls = CLASSES[i];
            Rect r = iconRects.get(i);
            boolean selected = i == currentIndex;
            boolean hovered = r.contains(mx, my);
            drawSlot(g, r, selected || hovered);
            g.fill(r.x + 2, r.y + 2, r.x + r.w - 2, r.y + r.h - 2, 0x44101018);
            if (selected) {
                g.fill(r.x, r.y + r.h - 2, r.x + r.w, r.y + r.h, cls.color);
            }
            g.drawCenteredString(font, cls.icon, r.x + r.w / 2, r.y + us(14), cls.color);
        }
    }

    private void drawStatsArea(GuiGraphics g) {
        PlayerClass cls = CLASSES[currentIndex];

        int leftX = ux(34);
        int rightX = ux(530);
        int chartCx = ux(320);
        int chartCy = uy(248);
        int chartR = us(74);

        g.drawCenteredString(font, "使用武器", leftX, uy(214), C_TEXT);
        g.drawCenteredString(font, getWeaponName(cls), leftX, uy(230), C_TEXT);

        drawRadar(g, chartCx, chartCy, chartR, classStats(cls));
        g.drawCenteredString(font, "單體攻擊", chartCx, chartCy - chartR - us(18), 0xFFEDEB62);
        g.drawString(font, "防禦能力", chartCx + chartR + us(8), chartCy - us(10), 0xFFEDEB62, false);
        g.drawString(font, "機動能力", chartCx + chartR + us(8), chartCy + us(38), 0xFFEDEB62, false);
        g.drawCenteredString(font, "輔助能力", chartCx, chartCy + chartR + us(10), 0xFFEDEB62);
        g.drawString(font, "生存能力", chartCx - chartR - us(96), chartCy + us(38), 0xFFEDEB62, false);
        g.drawString(font, "群體攻擊", chartCx - chartR - us(96), chartCy - us(10), 0xFFEDEB62, false);

        g.drawCenteredString(font, "操作難度", rightX, uy(214), C_TEXT);
        g.drawCenteredString(font, difficultyStars(cls), rightX, uy(230), C_TEXT);

        int descX = ux(30);
        int descY = uy(346);
        int descW = us(580);
        int descH = us(46);
        drawPanelBox(g, descX, descY, descW, descH);
        g.fill(descX + 2, descY + 2, descX + descW - 2, descY + descH - 2, 0x88161616);

        List<FormattedCharSequence> lines = font.split(Component.literal(cls.displayName + classFlavor(cls)), descW - us(10));
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            g.drawString(font, lines.get(i), descX + us(6), descY + us(6) + i * us(12), C_TEXT, false);
        }
    }

    private void drawFooter(GuiGraphics g, int mx, int my) {
        boolean hoverConfirm = confirmRect.contains(mx, my);
        drawButton(g, confirmRect, hoverConfirm, false);
        g.fill(confirmRect.x + 2, confirmRect.y + 2, confirmRect.x + confirmRect.w - 2, confirmRect.y + confirmRect.h - 2, 0x33582424);
        g.drawCenteredString(font, "確定職業", confirmRect.x + confirmRect.w / 2, confirmRect.y + us(7), C_TEXT);

        drawSmallButton(g, backRect, "返回", mx, my);
        g.drawCenteredString(font, "職業一旦選定後無法更改", ux(320), uy(330), C_WARN);
    }

    private void drawSmallButton(GuiGraphics g, Rect rect, String text, int mx, int my) {
        boolean hover = rect.contains(mx, my);
        int fg = C_TEXT;
        drawButton(g, rect, hover, false);
        g.drawCenteredString(font, text, rect.x + rect.w / 2, rect.y + us(7), fg);
    }

    private int ux(int baseX) {
        return layoutX + Math.round(baseX * layoutScale);
    }

    private int uy(int baseY) {
        return layoutY + Math.round(baseY * layoutScale);
    }

    private int us(int baseSize) {
        return Math.max(1, Math.round(baseSize * layoutScale));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) {
            return super.mouseClicked(mx, my, button);
        }

        if (prevRect.contains((int) mx, (int) my)) {
            currentIndex = (currentIndex - 1 + CLASSES.length) % CLASSES.length;
            selectedClass = CLASSES[currentIndex];
            return true;
        }

        if (nextRect.contains((int) mx, (int) my)) {
            currentIndex = (currentIndex + 1) % CLASSES.length;
            selectedClass = CLASSES[currentIndex];
            return true;
        }

        for (int i = 0; i < iconRects.size(); i++) {
            if (iconRects.get(i).contains((int) mx, (int) my)) {
                currentIndex = i;
                selectedClass = CLASSES[currentIndex];
                return true;
            }
        }

        if (confirmRect.contains((int) mx, (int) my)) {
            confirmSelection();
            return true;
        }

        if (backRect.contains((int) mx, (int) my)) {
            if (minecraft != null) {
                minecraft.setScreen(new MainMenuScreen());
            }
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    private void confirmSelection() {
        if (selectedClass == null) {
            return;
        }
        ModNetworking.sendToServer(new ClassSelectPacket(selectedClass));
        onClose();
    }

    private static int[] classStats(PlayerClass cls) {
        switch (cls) {
            case SWORDSMAN:
                return new int[]{7, 8, 5, 4, 7, 5};
            case MAGE:
                return new int[]{6, 3, 4, 7, 4, 9};
            case ARCHER:
                return new int[]{8, 4, 8, 3, 5, 5};
            case CULTIVATOR:
                return new int[]{6, 6, 6, 7, 7, 6};
            case SUMMONER:
                return new int[]{5, 4, 4, 8, 5, 8};
            case WHITE_MAGE:
                return new int[]{4, 6, 4, 9, 8, 5};
            case ASSASSIN:
                return new int[]{9, 3, 9, 2, 4, 4};
            default:
                return new int[]{1, 1, 1, 1, 1, 1};
        }
    }

    private static String getWeaponName(PlayerClass cls) {
        switch (cls) {
            case SWORDSMAN:
                return "劍";
            case MAGE:
                return "法杖";
            case ARCHER:
                return "弓";
            case CULTIVATOR:
                return "拳套";
            case SUMMONER:
                return "法書";
            case WHITE_MAGE:
                return "聖杖";
            case ASSASSIN:
                return "短刃";
            default:
                return "-";
        }
    }

    private static String classFlavor(PlayerClass cls) {
        switch (cls) {
            case SWORDSMAN:
                return "擁有強大的防禦與生存能力，可持續壓制敵人並保護隊友。";
            case MAGE:
                return "擁有高額魔法爆發，擅長群體清場與控制節奏。";
            case ARCHER:
                return "以遠程精準輸出見長，具備優秀拉打能力。";
            case CULTIVATOR:
                return "攻守均衡，持續作戰能力優異。";
            case SUMMONER:
                return "透過召喚物創造壓力，適合多目標戰鬥。";
            case WHITE_MAGE:
                return "以治療與護佑見長，提供穩定續航。";
            case ASSASSIN:
                return "具備高機動與高爆發，擅長快速斬首。";
            default:
                return "尚未選擇職業。";
        }
    }

    private static String difficultyStars(PlayerClass cls) {
        switch (cls) {
            case SWORDSMAN:
                return "★★";
            case MAGE:
                return "★★★";
            case ARCHER:
                return "★★★";
            case CULTIVATOR:
                return "★★★";
            case SUMMONER:
                return "★★★★";
            case WHITE_MAGE:
                return "★★★";
            case ASSASSIN:
                return "★★★★★";
            default:
                return "★";
        }
    }

    private void drawRadar(GuiGraphics g, int cx, int cy, int radius, int[] values) {
        for (int r = radius / 4; r <= radius; r += radius / 4) {
            drawHex(g, cx, cy, r, 0x552F8FD8);
        }
        drawHex(g, cx, cy, radius, 0xFF2A6EA8);

        int[] px = new int[6];
        int[] py = new int[6];
        for (int i = 0; i < 6; i++) {
            double rad = Math.toRadians(-90 + i * 60);
            int len = (int) (radius * (Math.max(0, Math.min(10, values[i])) / 10.0));
            px[i] = cx + (int) (Math.cos(rad) * len);
            py[i] = cy + (int) (Math.sin(rad) * len);
            drawLine(g, cx, cy, cx + (int) (Math.cos(rad) * radius), cy + (int) (Math.sin(rad) * radius), 0x662F8FD8);
        }
        fillPolygon(g, px, py, 0x6631A4F0);
        drawPolygon(g, px, py, 0xFF66CCFF);
    }

    private static void drawHex(GuiGraphics g, int cx, int cy, int r, int color) {
        int[] x = new int[6];
        int[] y = new int[6];
        for (int i = 0; i < 6; i++) {
            double rad = Math.toRadians(-90 + i * 60);
            x[i] = cx + (int) (Math.cos(rad) * r);
            y[i] = cy + (int) (Math.sin(rad) * r);
        }
        drawPolygon(g, x, y, color);
    }

    private static void drawPolygon(GuiGraphics g, int[] x, int[] y, int color) {
        for (int i = 0; i < x.length; i++) {
            int j = (i + 1) % x.length;
            drawLine(g, x[i], y[i], x[j], y[j], color);
        }
    }

    private static void fillPolygon(GuiGraphics g, int[] x, int[] y, int color) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int yv : y) {
            minY = Math.min(minY, yv);
            maxY = Math.max(maxY, yv);
        }

        for (int scanY = minY; scanY <= maxY; scanY++) {
            int[] hits = new int[12];
            int hitCount = 0;
            for (int i = 0; i < x.length; i++) {
                int j = (i + 1) % x.length;
                int y1 = y[i];
                int y2 = y[j];
                int x1 = x[i];
                int x2 = x[j];
                if ((scanY >= y1 && scanY < y2) || (scanY >= y2 && scanY < y1)) {
                    int hitX = x1 + (scanY - y1) * (x2 - x1) / (y2 - y1);
                    hits[hitCount++] = hitX;
                }
            }
            for (int i = 0; i + 1 < hitCount; i += 2) {
                int a = Math.min(hits[i], hits[i + 1]);
                int b = Math.max(hits[i], hits[i + 1]);
                g.fill(a, scanY, b + 1, scanY + 1, color);
            }
        }
    }

    private static void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1;
        int y = y1;

        while (true) {
            g.fill(x, y, x + 1, y + 1, color);
            if (x == x2 && y == y2) {
                break;
            }
            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private static void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private static void blitUI(GuiGraphics g, int x, int y, int u, int v, int w, int h) {
        g.blit(UI_TEX, x, y, u, v, w, h, UI_TEX_W, UI_TEX_H);
    }

    private static void drawPanelBox(GuiGraphics g, int x, int y, int w, int h) {
        int corner = 4;
        int edgeW = Math.max(0, w - corner * 2);
        int edgeH = Math.max(0, h - corner * 2);

        blitUI(g, x, y, 0, 0, corner, corner);
        blitUI(g, x + w - corner, y, 12, 0, corner, corner);
        blitUI(g, x, y + h - corner, 0, 12, corner, corner);
        blitUI(g, x + w - corner, y + h - corner, 12, 12, corner, corner);

        for (int dx = 0; dx < edgeW; dx += 8) {
            int tw = Math.min(8, edgeW - dx);
            blitUI(g, x + corner + dx, y, 4, 0, tw, corner);
            blitUI(g, x + corner + dx, y + h - corner, 4, 12, tw, corner);
        }

        for (int dy = 0; dy < edgeH; dy += 8) {
            int th = Math.min(8, edgeH - dy);
            blitUI(g, x, y + corner + dy, 0, 4, corner, th);
            blitUI(g, x + w - corner, y + corner + dy, 12, 4, corner, th);
        }

        for (int dy = 0; dy < edgeH; dy += 8) {
            int th = Math.min(8, edgeH - dy);
            for (int dx = 0; dx < edgeW; dx += 8) {
                int tw = Math.min(8, edgeW - dx);
                blitUI(g, x + corner + dx, y + corner + dy, 4, 4, tw, th);
            }
        }
    }

    private static void drawButton(GuiGraphics g, Rect r, boolean hovered, boolean disabled) {
        int u = disabled ? 48 : (hovered ? 32 : 16);
        int v = 0;
        int corner = 4;
        int edgeW = Math.max(0, r.w - corner * 2);
        int edgeH = Math.max(0, r.h - corner * 2);

        blitUI(g, r.x, r.y, u, v, corner, corner);
        blitUI(g, r.x + r.w - corner, r.y, u + 12, v, corner, corner);
        blitUI(g, r.x, r.y + r.h - corner, u, v + 12, corner, corner);
        blitUI(g, r.x + r.w - corner, r.y + r.h - corner, u + 12, v + 12, corner, corner);

        for (int dx = 0; dx < edgeW; dx += 8) {
            int tw = Math.min(8, edgeW - dx);
            blitUI(g, r.x + corner + dx, r.y, u + 4, v, tw, corner);
            blitUI(g, r.x + corner + dx, r.y + r.h - corner, u + 4, v + 12, tw, corner);
        }
        for (int dy = 0; dy < edgeH; dy += 8) {
            int th = Math.min(8, edgeH - dy);
            blitUI(g, r.x, r.y + corner + dy, u, v + 4, corner, th);
            blitUI(g, r.x + r.w - corner, r.y + corner + dy, u + 12, v + 4, corner, th);
        }
        for (int dy = 0; dy < edgeH; dy += 8) {
            int th = Math.min(8, edgeH - dy);
            for (int dx = 0; dx < edgeW; dx += 8) {
                int tw = Math.min(8, edgeW - dx);
                blitUI(g, r.x + corner + dx, r.y + corner + dy, u + 4, v + 4, tw, th);
            }
        }
    }

    private static void drawSlot(GuiGraphics g, Rect r, boolean selected) {
        int u = selected ? 18 : 0;
        int v = 16;
        int tile = 18;
        int x = r.x + (r.w - tile) / 2;
        int y = r.y + (r.h - tile) / 2;
        blitUI(g, x, y, u, v, tile, tile);
    }

    private static final class Rect {
        final int x;
        final int y;
        final int w;
        final int h;

        private Rect(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        private boolean contains(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }
}

