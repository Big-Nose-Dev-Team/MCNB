package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.client.gui.Libs.HBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ScaledLabel;
import com.yichenxbohan.mcnb.client.gui.Libs.ScrollVBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedButton;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedPanelScreen;
import com.yichenxbohan.mcnb.client.gui.Libs.UITheme;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MainMenuScreen extends ThemedPanelScreen {

    public MainMenuScreen() {
        super(Component.literal("MCNB 選單"), 420, 260, UITheme.DEFAULT);
    }

    @Override
    protected void buildContent(ScrollVBox content) {
        Minecraft mc = Minecraft.getInstance();
        PlayerClass cls = PlayerClass.NONE;
        if (mc.player != null) {
            cls = mc.player.getCapability(ModCapabilities.PLAYER_CLASS)
                    .map(cap -> cap.getPlayerClass())
                    .orElse(PlayerClass.NONE);
        }
        final PlayerClass finalCls = cls;

        content.addChild(new ScaledLabel(0, 18,
                Component.literal(cls == PlayerClass.NONE
                        ? "職業: 未選擇"
                        : "職業: " + cls.icon + " " + cls.displayName),
                cls == PlayerClass.NONE ? theme.labelText() : cls.color));
        content.addChild(new ScaledLabel(0, 18,
                Component.literal("所有頁面已統一為 Lib 版型，會依視窗比例自動縮放"),
                theme.labelText()));

        addMenuEntry(content,
                "職業系統",
                finalCls == PlayerClass.NONE ? "尚未選擇職業，進入職業選擇" : "查看屬性與點數分配",
                finalCls == PlayerClass.NONE ? "前往" : "開啟",
                () -> mc.setScreen(finalCls == PlayerClass.NONE ? new ClassSelectScreen() : new StatsScreen()));

        addMenuEntry(content,
                "職業技能",
                finalCls == PlayerClass.NONE ? "請先選擇職業後解鎖技能頁" : "開啟技能加點與分支選擇",
                finalCls == PlayerClass.NONE ? "選職業" : "開啟",
                () -> mc.setScreen(finalCls == PlayerClass.NONE ? new ClassSelectScreen() : new SkillClassScreen()));
    }

    private void addMenuEntry(ScrollVBox content, String title, String desc, String actionText, Runnable action) {
        HBox row = new HBox(0, 0, 0, 34, 6, 4, 4);
        row.addFill(new ScaledLabel(0, 16, Component.literal(title + " - " + desc), theme.valueText()));
        row.addRight(new ThemedButton(84, 24, Component.literal(actionText), theme, b -> action.run()));
        content.addChild(row);
    }
}

