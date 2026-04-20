package com.yichenxbohan.mcnb.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class ThemedPanelScreen extends Screen {
    protected final UITheme theme;
    protected final int panelW, panelH;
    protected int panelX, panelY;

    protected ScrollVBox contentBox;

    public ThemedPanelScreen(Component title, int width, int height, UITheme theme) {
        super(title);
        this.panelW = width;
        this.panelH = height;
        this.theme = theme;
    }

    @Override
    protected void init() {
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        // Content area leaves room for borders (2px) and header (22px)
        contentBox = new ScrollVBox(panelX + 4, panelY + 26, panelW - 8, panelH - 30, 4);

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
        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + 22, theme.headerBg());
        DrawUtils.drawBorderedBox(guiGraphics, panelX, panelY, panelW, 22, 0, theme.border()); // Header border only

        // Title
        guiGraphics.drawCenteredString(font, getTitle().getString(), panelX + panelW / 2, panelY + 7, theme.titleText());

        super.render(guiGraphics, mx, my, pt); // Renders the ScrollVBox and its children
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Or true depending on your needs
    }
}