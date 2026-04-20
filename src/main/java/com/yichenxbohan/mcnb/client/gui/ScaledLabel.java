package com.yichenxbohan.mcnb.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

public class ScaledLabel extends AbstractWidget {
    private final int color;

    public ScaledLabel(int w, int h, Component text, int color) {
        super(0, 0, w, h, text);
        this.color = color;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mx, int my, float pt) {
        DrawUtils.drawScaledText(guiGraphics, Minecraft.getInstance().font, getMessage().getString(),
                getX(), getY() + (height - 8) / 2, width, color);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput out) {}
}