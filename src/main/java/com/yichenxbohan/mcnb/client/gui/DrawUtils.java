package com.yichenxbohan.mcnb.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class DrawUtils {

    // Shrinks text to fit within maxWidth, otherwise draws normally
    public static void drawScaledText(GuiGraphics guiGraphics, Font font, String text, int x, int y, int maxWidth, int color) {
        int textWidth = font.width(text);
        if (textWidth <= maxWidth) {
            guiGraphics.drawString(font, text, x, y, color, false);
            return;
        }

        float scale = (float) maxWidth / textWidth;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    // Draws a filled box with a 1px border
    public static void drawBorderedBox(GuiGraphics guiGraphics, int x, int y, int width, int height, int bgColor, int borderColor) {
        guiGraphics.fill(x, y, x + width, y + height, bgColor);
        guiGraphics.hLine(x, x + width - 1, y, borderColor);
        guiGraphics.hLine(x, x + width - 1, y + height - 1, borderColor);
        guiGraphics.vLine(x, y, y + height - 1, borderColor);
        guiGraphics.vLine(x + width - 1, y, y + height - 1, borderColor);
    }
}