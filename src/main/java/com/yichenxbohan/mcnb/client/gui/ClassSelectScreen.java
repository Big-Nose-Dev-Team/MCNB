package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.network.ClassSelectPacket;
import com.yichenxbohan.mcnb.network.ModNetworking;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 職業選擇畫面（翻頁式）
 */
@OnlyIn(Dist.CLIENT)
public class ClassSelectScreen extends Screen {

    private static final int PANEL_MAX_W = 360;
    private static final int PANEL_MAX_H = 390;
    private static final int BOTTOM_SAFE = 48;
    private static final int TOP_SAFE    = 8;

    private static final int CARD_W = 148;
    private static final int CARD_H = 66;
    private static final int CARD_GAP_X = 10;
    private static final int CARD_GAP_Y = 8;

    // 底部按鈕列高度
    private static final int BOTTOM_BAR_H = 32;
    // 翻頁按鈕尺寸
    private static final int PAGE_BTN_W = 28;
    private static final int PAGE_BTN_H = 20;

    // 顏色
    private static final int C_BG       = 0xE8101014;
    private static final int C_BORDER   = 0xFF3A4A6B;
    private static final int C_GLOW     = 0x554A7FBF;
    private static final int C_HEADER   = 0xCC1A2540;
    private static final int C_TITLE    = 0xFFE8D87A;
    private static final int C_CARD_BG  = 0xCC1C2030;
    private static final int C_CARD_SEL = 0xCC2A3A60;
    private static final int C_CARD_HOV = 0xCC252840;
    private static final int C_DESC     = 0xFFAABBCC;
    private static final int C_WARNING  = 0xFFFF8888;
    private static final int C_CONFIRM  = 0xFF55FF88;
    private static final int C_CONFIRM_HOV = 0xFF88FFAA;
    private static final int C_BACK     = 0xFF888899;

    private int panelX, panelY;
    private int panelW, panelH;
    private PlayerClass hoveredClass = null;
    private PlayerClass selectedClass = null;

    // 確認 / 返回按鈕
    private int confirmX, confirmY, confirmW, confirmH;
    private int backX,    backY,    backW,    backH;

    // 翻頁按鈕
    private int prevBtnX, prevBtnY;
    private int nextBtnX, nextBtnY;

    private static final PlayerClass[] CLASSES = {
        PlayerClass.SWORDSMAN, PlayerClass.MAGE, PlayerClass.ARCHER,
        PlayerClass.CULTIVATOR, PlayerClass.SUMMONER, PlayerClass.WHITE_MAGE,
        PlayerClass.ASSASSIN
    };

    // 翻頁狀態
    private int currentPage = 0;
    private int cardsPerPage = 4; // 動態計算
    private int totalPages   = 1;

    // 當前頁的卡片佈局 [index][x, y, w, h]
    private int[][] cardRects;
    // 當前頁顯示的 CLASSES 索引
    private int[] pageIndices;

    // 卡片區域的 Y 起點與可用高度（init 計算）
    private int cardAreaY;
    private int cardAreaH;
    // 每列卡片寬度（動態）
    private int singleCardW;

    public ClassSelectScreen() {
        super(Component.literal("選擇職業"));
    }

    @Override
    protected void init() {
        int availH = height - TOP_SAFE - BOTTOM_SAFE;
        panelW = Math.min(width - 20, PANEL_MAX_W);
        panelH = Math.min(availH, PANEL_MAX_H);

        panelX = (width  - panelW) / 2;
        panelY = TOP_SAFE + (availH - panelH) / 2;

        // 卡片區域：標題列 36px + 10px padding，底部留 BOTTOM_BAR_H
        cardAreaY = panelY + 46;
        cardAreaH = panelH - 46 - BOTTOM_BAR_H;

        // 計算每列寬度
        int usableW = panelW - 16;
        singleCardW = Math.min(CARD_W, (usableW - CARD_GAP_X) / 2);

        // 每頁可容納幾行，每行 2 張
        int rowsPerPage = Math.max(1, cardAreaH / (CARD_H + CARD_GAP_Y));
        cardsPerPage = rowsPerPage * 2;

        totalPages = (int) Math.ceil((double) CLASSES.length / cardsPerPage);
        if (currentPage >= totalPages) currentPage = 0;

        buildPageLayout();

        // 底部按鈕列的 Y
        int barY = panelY + panelH - BOTTOM_BAR_H;

        // 翻頁按鈕（緊靠面板兩側）
        prevBtnX = panelX + 6;
        prevBtnY = barY + (BOTTOM_BAR_H - PAGE_BTN_H) / 2;
        nextBtnX = panelX + panelW - PAGE_BTN_W - 6;
        nextBtnY = prevBtnY;

        // 確認 / 返回按鈕（居中）
        confirmW = 100; confirmH = 20;
        backW    = 80;  backH    = 20;
        int totalBtnW = confirmW + 8 + backW;
        int btnStartX = panelX + (panelW - totalBtnW) / 2;
        confirmX = btnStartX;
        confirmY = barY + (BOTTOM_BAR_H - confirmH) / 2;
        backX    = btnStartX + confirmW + 8;
        backY    = confirmY;
    }

    /** 根據 currentPage 重新計算 cardRects 和 pageIndices */
    private void buildPageLayout() {
        int startIdx = currentPage * cardsPerPage;
        int endIdx   = Math.min(startIdx + cardsPerPage, CLASSES.length);
        int count    = endIdx - startIdx;

        pageIndices = new int[count];
        cardRects   = new int[count][4];

        int startX = panelX + (panelW - (singleCardW * 2 + CARD_GAP_X)) / 2;

        for (int i = 0; i < count; i++) {
            pageIndices[i] = startIdx + i;
            int col = i % 2;
            int row = i / 2;
            cardRects[i][0] = startX + col * (singleCardW + CARD_GAP_X);
            cardRects[i][1] = cardAreaY + row * (CARD_H + CARD_GAP_Y);
            cardRects[i][2] = singleCardW;
            cardRects[i][3] = CARD_H;
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0xAA000000);
        updateHover(mx, my);
        drawPanel(g, mx, my);
        super.render(g, mx, my, pt);
    }

    private void updateHover(int mx, int my) {
        hoveredClass = null;
        for (int i = 0; i < cardRects.length; i++) {
            int[] r = cardRects[i];
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                hoveredClass = CLASSES[pageIndices[i]];
                return;
            }
        }
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
        g.drawCenteredString(font, "✦ 選擇你的職業 ✦", x + w / 2, y + 7, C_TITLE);
        g.drawCenteredString(font, "職業一旦選定便無法更改，請謹慎選擇", x + w / 2, y + 21, C_WARNING);

        // 職業卡片（當前頁）
        for (int i = 0; i < cardRects.length; i++) {
            drawClassCard(g, i);
        }

        // 說明區（懸停或已選）
        PlayerClass toShow = selectedClass != null ? selectedClass : hoveredClass;
        if (toShow != null) {
            drawDescription(g, toShow, x, y, w, h);
        }

        // ── 底部按鈕列分隔線 ──
        int barY = y + h - BOTTOM_BAR_H;
        g.fill(x, barY, x + w, barY + 1, C_BORDER);

        // ── 翻頁按鈕 ──
        boolean hasPrev = currentPage > 0;
        boolean hasNext = currentPage < totalPages - 1;
        drawPageBtn(g, prevBtnX, prevBtnY, "‹", hasPrev,
                    mx >= prevBtnX && mx < prevBtnX + PAGE_BTN_W &&
                    my >= prevBtnY && my < prevBtnY + PAGE_BTN_H);
        drawPageBtn(g, nextBtnX, nextBtnY, "›", hasNext,
                    mx >= nextBtnX && mx < nextBtnX + PAGE_BTN_W &&
                    my >= nextBtnY && my < nextBtnY + PAGE_BTN_H);

        // 頁碼
        String pageLabel = (currentPage + 1) + " / " + totalPages;
        // 置於兩個翻頁按鈕之間（底部列左側）
        int pageLabelX = prevBtnX + PAGE_BTN_W + 4;
        g.drawString(font, pageLabel, pageLabelX, prevBtnY + (PAGE_BTN_H - 8) / 2, 0xFF9999AA, false);

        // ── 確認按鈕 ──
        boolean canConfirm = selectedClass != null;
        boolean hoverConfirm = mx >= confirmX && mx < confirmX + confirmW &&
                               my >= confirmY && my < confirmY + confirmH;
        int confirmBg  = canConfirm ? (hoverConfirm ? 0xCC224422 : 0xCC1A3318) : 0xCC222222;
        int confirmTxt = canConfirm ? (hoverConfirm ? C_CONFIRM_HOV : C_CONFIRM) : 0xFF666666;
        g.fill(confirmX, confirmY, confirmX + confirmW, confirmY + confirmH, confirmBg);
        drawBorder(g, confirmX, confirmY, confirmX + confirmW, confirmY + confirmH,
                   canConfirm ? C_CONFIRM : 0xFF444455);
        String confirmLabel = canConfirm ? "✔ 確認選擇" : "請先選擇職業";
        g.drawCenteredString(font, confirmLabel, confirmX + confirmW / 2, confirmY + 6, confirmTxt);

        // ── 返回按鈕 ──
        boolean hoverBack = mx >= backX && mx < backX + backW && my >= backY && my < backY + backH;
        g.fill(backX, backY, backX + backW, backY + backH, hoverBack ? 0xCC302830 : 0xCC201820);
        drawBorder(g, backX, backY, backX + backW, backY + backH, C_BACK);
        g.drawCenteredString(font, "← 返回", backX + backW / 2, backY + 6,
                             hoverBack ? 0xFFCCCCDD : C_BACK);
    }

    private void drawPageBtn(GuiGraphics g, int bx, int by, String label,
                             boolean active, boolean hovered) {
        int bg  = active ? (hovered ? 0xCC2A3A60 : 0xCC1C2030) : 0xCC181818;
        int col = active ? (hovered ? C_TITLE : 0xFFCCCCDD) : 0xFF444455;
        g.fill(bx, by, bx + PAGE_BTN_W, by + PAGE_BTN_H, bg);
        drawBorder(g, bx, by, bx + PAGE_BTN_W, by + PAGE_BTN_H, active ? C_BORDER : 0xFF333344);
        g.drawCenteredString(font, label, bx + PAGE_BTN_W / 2, by + (PAGE_BTN_H - 8) / 2, col);
    }

    private void drawClassCard(GuiGraphics g, int localIdx) {
        int[] r   = cardRects[localIdx];
        int cx = r[0], cy = r[1], cw = r[2], ch = r[3];
        PlayerClass cls = CLASSES[pageIndices[localIdx]];

        boolean isSelected = cls == selectedClass;
        boolean isHovered  = cls == hoveredClass;

        int bg     = isSelected ? C_CARD_SEL : (isHovered ? C_CARD_HOV : C_CARD_BG);
        int border = isSelected ? cls.color : (isHovered ? blendColor(cls.color, C_BORDER, 0.5f) : C_BORDER);

        g.fill(cx, cy, cx + cw, cy + ch, bg);
        drawBorder(g, cx, cy, cx + cw, cy + ch, border);
        if (isSelected) g.fill(cx, cy, cx + 3, cy + ch, cls.color);

        int iconColor = isSelected ? cls.color : blendColor(cls.color, 0xFFFFFFFF, 0.4f);
        g.drawString(font, cls.icon, cx + 8, cy + 8, iconColor, true);
        g.drawString(font, cls.displayName, cx + 22, cy + 8, isSelected ? cls.color : 0xFFEEEEEE, true);

        String desc = cls.description;
        if (font.width(desc) > cw - 16) {
            desc = font.substrByWidth(Component.literal(desc), cw - 28).getString() + "...";
        }
        g.drawString(font, desc, cx + 8, cy + 24, C_DESC, false);

        if (isSelected) {
            g.drawString(font, "✔ 已選擇", cx + 8, cy + ch - 14, C_CONFIRM, false);
        }
    }

    private void drawDescription(GuiGraphics g, PlayerClass cls, int px, int py, int pw, int ph) {
        // 說明欄放在卡片區底部與底部按鈕列之間
        int dw = pw - 16;
        int dx = px + 8;
        int dy = cardAreaY + cardAreaH - 26;
        int dh = 24;

        g.fill(dx, dy, dx + dw, dy + dh, 0xAA111120);
        drawBorder(g, dx, dy, dx + dw, dy + dh, cls.color);
        g.drawString(font, cls.icon + " " + cls.displayName, dx + 6, dy + 4, cls.color, true);
        g.drawString(font, cls.description, dx + 6, dy + 14, C_DESC, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);

        // 翻頁：上一頁
        if (currentPage > 0 &&
            mx >= prevBtnX && mx < prevBtnX + PAGE_BTN_W &&
            my >= prevBtnY && my < prevBtnY + PAGE_BTN_H) {
            currentPage--;
            buildPageLayout();
            return true;
        }

        // 翻頁：下一頁
        if (currentPage < totalPages - 1 &&
            mx >= nextBtnX && mx < nextBtnX + PAGE_BTN_W &&
            my >= nextBtnY && my < nextBtnY + PAGE_BTN_H) {
            currentPage++;
            buildPageLayout();
            return true;
        }

        // 點卡片
        for (int i = 0; i < cardRects.length; i++) {
            int[] r = cardRects[i];
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                PlayerClass clicked = CLASSES[pageIndices[i]];
                selectedClass = (selectedClass == clicked) ? null : clicked;
                return true;
            }
        }

        // 確認按鈕
        if (selectedClass != null &&
            mx >= confirmX && mx < confirmX + confirmW &&
            my >= confirmY && my < confirmY + confirmH) {
            confirmSelection();
            return true;
        }

        // 返回按鈕
        if (mx >= backX && mx < backX + backW &&
            my >= backY && my < backY + backH) {
            onClose();
            if (minecraft != null) minecraft.setScreen(new MainMenuScreen());
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    private void confirmSelection() {
        if (selectedClass == null) return;
        ModNetworking.sendToServer(new ClassSelectPacket(selectedClass));
        onClose();
    }

    // ── 工具 ──

    private void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1,     x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2,     color);
        g.fill(x1, y1,     x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2,     color);
    }

    private static int blendColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF, aa = (a >> 24) & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF, ba = (b >> 24) & 0xFF;
        int r  = (int)(ar + (br - ar) * t);
        int gv = (int)(ag + (bg - ag) * t);
        int bv = (int)(ab + (bb - ab) * t);
        int av = (int)(aa + (ba - aa) * t);
        return (av << 24) | (r << 16) | (gv << 8) | bv;
    }
}

