package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.client.gui.UITheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ThemedButton extends Button {
    private final UITheme theme;

    public ThemedButton(int w, int h, Component msg, UITheme theme, OnPress onPress) {
        super(0, 0, w, h, msg, onPress, Button.DEFAULT_NARRATION);
        this.theme = theme;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mx, int my, float pt) {
        int bg = !active ? theme.btnDisabled() : (isHovered() ? theme.btnHover() : theme.btnBg());
        int border = active && isHovered() ? theme.titleText() : theme.border();

        DrawUtils.drawBorderedBox(guiGraphics, getX(), getY(), width, height, bg, border);
        int textColor = !active ? 0xFF666677 : (isHovered() ? theme.titleText() : theme.valueText());

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, getMessage().getString(),
                getX() + width / 2, getY() + (height - 8) / 2, textColor);
    }
}