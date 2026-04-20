package com.yichenxbohan.mcnb.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class HBox extends AbstractWidget {
    private final List<AbstractWidget> leftChildren = new ArrayList<>();
    private final List<AbstractWidget> rightChildren = new ArrayList<>();
    private AbstractWidget fillChild; // optional: takes remaining horizontal space

    private final int paddingLeft;
    private final int paddingRight;
    private final int gap;

    public HBox(int x, int y, int width, int height, int gap) {
        this(x, y, width, height, gap, 0, 0);
    }

    public HBox(int x, int y, int width, int height, int gap, int paddingLeft, int paddingRight) {
        super(x, y, width, height, Component.empty());
        this.gap = gap;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
    }

    public <T extends AbstractWidget> T addLeft(T child) {
        leftChildren.add(child);
        return child;
    }

    public <T extends AbstractWidget> T addRight(T child) {
        rightChildren.add(child);
        return child;
    }

    /** A single widget that expands to remaining width between left and right groups. */
    public <T extends AbstractWidget> T addFill(T child) {
        this.fillChild = child;
        return child;
    }

    /** Recompute all child positions/sizes. Call after size changes or child changes. */
    public void layoutChildren() {
        int leftX = getX() + paddingLeft;
        int rightX = getX() + width - paddingRight;

        // Place right side from right -> left
        for (int i = rightChildren.size() - 1; i >= 0; i--) {
            AbstractWidget child = rightChildren.get(i);
            rightX -= child.getWidth();
            child.setX(rightX);
            child.setY(centerY(child.getHeight()));
            rightX -= gap;
        }

        // Place left side from left -> right
        for (AbstractWidget child : leftChildren) {
            child.setX(leftX);
            child.setY(centerY(child.getHeight()));
            leftX += child.getWidth() + gap;
        }

        // Fill child uses remaining space
        if (fillChild != null) {
            int fillX = leftX;
            int fillRight = rightX + gap; // undo trailing gap on right side
            int fillWidth = Math.max(0, fillRight - fillX);
            fillChild.setX(fillX);
            fillChild.setY(centerY(fillChild.getHeight()));
            fillChild.setWidth(fillWidth);
        }
    }

    private int centerY(int childHeight) {
        return getY() + (height - childHeight) / 2;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        layoutChildren();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        layoutChildren();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        layoutChildren();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        layoutChildren();
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        // HBox itself doesn't draw background by default; children do.
        for (AbstractWidget child : leftChildren) child.render(gg, mouseX, mouseY, partialTick);
        if (fillChild != null) fillChild.render(gg, mouseX, mouseY, partialTick);
        for (AbstractWidget child : rightChildren) child.render(gg, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AbstractWidget child : leftChildren) {
            if (child.mouseClicked(mouseX, mouseY, button)) return true;
        }
        if (fillChild != null && fillChild.mouseClicked(mouseX, mouseY, button)) return true;
        for (AbstractWidget child : rightChildren) {
            if (child.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {}
}