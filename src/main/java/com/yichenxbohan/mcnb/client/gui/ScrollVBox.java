package com.yichenxbohan.mcnb.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class ScrollVBox extends AbstractWidget {
    private final List<AbstractWidget> children = new ArrayList<>();

    private final int gap;
    private final int contentPadding;
    private final int scrollbarWidth;
    private final int scrollbarGap;

    private double scrollY = 0;
    private int contentHeight = 0;

    public ScrollVBox(int x, int y, int w, int h, int gap) {
        this(x, y, w, h, gap, 4, 3, 3);
    }

    public ScrollVBox(int x, int y, int w, int h, int gap, int contentPadding, int scrollbarWidth, int scrollbarGap) {
        super(x, y, w, h, Component.empty());
        this.gap = gap;
        this.contentPadding = contentPadding;
        this.scrollbarWidth = scrollbarWidth;
        this.scrollbarGap = scrollbarGap;
    }

    /** Width available for row content (already subtracts left/right padding + scrollbar lane). */
    public int getContentWidth() {
        return width - (contentPadding * 2) - scrollbarWidth - scrollbarGap;
    }

    public int getContentX() {
        return getX() + contentPadding;
    }

    public int getContentTopY() {
        return getY() + contentPadding;
    }

    public int getContentBottomY() {
        return getY() + height - contentPadding;
    }

    public <T extends AbstractWidget> T addChild(T widget) {
        int childX = getContentX();
        int childY = getContentTopY() + contentHeight;

        widget.setX(childX);
        widget.setY(childY);
        widget.setWidth(getContentWidth());

        children.add(widget);
        contentHeight += widget.getHeight() + gap;
        return widget;
    }

    private int getViewportHeight() {
        return Math.max(0, (height - contentPadding * 2));
    }

    private int getMaxScroll() {
        return Math.max(0, contentHeight - getViewportHeight());
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float pt) {
        int clipLeft = getContentX();
        int clipTop = getContentTopY();
        int clipRight = getContentX() + getContentWidth();
        int clipBottom = getContentBottomY();

        gg.enableScissor(clipLeft, clipTop, clipRight, clipBottom);

        gg.pose().pushPose();
        gg.pose().translate(0, -scrollY, 0);

        int adjustedMouseY = (int) (mouseY + scrollY);
        for (AbstractWidget child : children) {
            child.render(gg, mouseX, adjustedMouseY, pt);
        }

        gg.pose().popPose();
        gg.disableScissor();

        drawScrollbar(gg);
    }

    private void drawScrollbar(GuiGraphics gg) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) return;

        int trackX = getX() + width - contentPadding - scrollbarWidth;
        int trackTop = getContentTopY();
        int trackHeight = getViewportHeight();
        int trackBottom = trackTop + trackHeight;

        gg.fill(trackX, trackTop, trackX + scrollbarWidth, trackBottom, 0x442A3344);

        float ratio = (float) trackHeight / (trackHeight + maxScroll);
        int thumbHeight = Math.max(16, (int) (trackHeight * ratio));
        int travel = trackHeight - thumbHeight;
        int thumbY = trackTop + (int) (travel * (scrollY / maxScroll));

        gg.fill(trackX, thumbY, trackX + scrollbarWidth, thumbY + thumbHeight, 0xAA9BB6FF);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (!isMouseOver(mx, my)) return false;
        scrollY = Mth.clamp(scrollY - (delta * 14), 0, getMaxScroll());
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) { }
}