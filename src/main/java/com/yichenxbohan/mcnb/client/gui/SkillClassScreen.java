package com.yichenxbohan.mcnb.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkillClassScreen extends ThemedPanelScreen {
    public SkillClassScreen() {
        super(Component.literal("技能"), 300, 200, UITheme.DEFAULT);
    }

    @Override
    protected void buildContent(ScrollVBox content) {
        HBox row = new HBox(0, 0, 0, 24, 6, 4, 4); // width assigned later by ScrollVBox
        row.addFill(new ScaledLabel(0, 20, Component.literal("Very long setting name..."), theme.labelText()));
        row.addRight(new ThemedButton(70, 18, Component.literal("綁定按鍵"), theme, b -> {}));

        content.addChild(row);
    }
}

