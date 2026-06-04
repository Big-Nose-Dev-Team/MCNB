package com.yichenxbohan.mcnb.client.gui.Libs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class ThemedPanelScreen extends Screen {
    private static final int SAFE_TOP = 8;
    private static final int SAFE_BOTTOM = 48;

    protected final UITheme theme;
    protected final int designPanelW;
    protected final int designPanelH;
    protected int panelX, panelY;
    protected int panelW, panelH;

    protected int headerHeight;
    protected int panelPadding;

    protected ScrollVBox contentBox;

    public ThemedPanelScreen(Component title, int width, int height, UITheme theme) {
        super(title);
        this.designPanelW = width;
        this.designPanelH = height;
        this.theme = theme;
    }

    @Override
    protected void init() {
        int availW = Math.max(160, this.width - 24);
        int safeHeight = Math.max(120, this.height - SAFE_TOP - SAFE_BOTTOM);
        int availH = Math.max(120, safeHeight);

        float scale = Math.min(availW / (float) designPanelW, availH / (float) designPanelH);
        panelW = Math.max(160, Math.round(designPanelW * scale));
        panelH = Math.max(120, Math.round(designPanelH * scale));

        panelX = (this.width - panelW) / 2;
        panelY = SAFE_TOP + (availH - panelH) / 2;

        headerHeight = Math.max(20, Math.min(34, Math.round(panelH * 0.11f)));
        panelPadding = Math.max(4, Math.round(Math.min(panelW, panelH) * 0.015f));

        int contentX = panelX + panelPadding;
        int contentY = panelY + headerHeight + panelPadding;
        int contentW = panelW - panelPadding * 2;
        int contentH = panelH - headerHeight - panelPadding * 2;

        int rowGap = Math.max(4, Math.round(panelH * 0.012f));
        contentBox = new ScrollVBox(contentX, contentY, contentW, Math.max(24, contentH), rowGap);

        buildContent(contentBox); // Let subclasses add their items!

        this.addRenderableWidget(contentBox);
    }

    // Subclasses implement this to add rows, buttons, etc.
    protected abstract void buildContent(ScrollVBox content);

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mx, int my, float pt) {
        // Darken outside world
        guiGraphics.fill(0, 0, width, height, 0x88000000);

        // Draw Main Panel
        DrawUtils.drawBorderedBox(guiGraphics, panelX, panelY, panelW, panelH, theme.bg(), theme.border());

        // Draw Header
        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + headerHeight, theme.headerBg());
        DrawUtils.drawBorderedBox(guiGraphics, panelX, panelY, panelW, headerHeight, 0, theme.border());

        // Title
        guiGraphics.drawCenteredString(font, getTitle().getString(), panelX + panelW / 2, panelY + (headerHeight - 8) / 2, theme.titleText());

        super.render(guiGraphics, mx, my, pt); // Renders the ScrollVBox and its children
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Or true depending on your needs
    }
}