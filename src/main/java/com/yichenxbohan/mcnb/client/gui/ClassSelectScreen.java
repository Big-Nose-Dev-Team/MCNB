package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.client.gui.Libs.HBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ScaledLabel;
import com.yichenxbohan.mcnb.client.gui.Libs.ScrollVBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedButton;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedPanelScreen;
import com.yichenxbohan.mcnb.client.gui.Libs.UITheme;
import com.yichenxbohan.mcnb.network.ClassSelectPacket;
import com.yichenxbohan.mcnb.network.ModNetworking;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClassSelectScreen extends ThemedPanelScreen {

    private static final PlayerClass[] CLASSES = {
            PlayerClass.SWORDSMAN,
            PlayerClass.MAGE,
            PlayerClass.ARCHER,
            PlayerClass.SUMMONER,
            PlayerClass.WHITE_MAGE,
            PlayerClass.ASSASSIN
    };

    private final int currentIndex;

    public ClassSelectScreen() {
        this(0);
    }

    private ClassSelectScreen(int currentIndex) {
        super(Component.literal("選擇職業"), 760, 460, UITheme.DEFAULT);
        this.currentIndex = Math.max(0, Math.min(CLASSES.length - 1, currentIndex));
    }

    @Override
    protected void buildContent(ScrollVBox content) {
        PlayerClass selectedClass = CLASSES[currentIndex];

        HBox nav = new HBox(0, 0, 0, 32, 6, 2, 2);
        nav.addLeft(new ThemedButton(52, 24, Component.literal("<<"), theme,
                b -> reopen(currentIndex - 1)));
        nav.addFill(new ScaledLabel(0, 18,
                Component.literal(selectedClass.icon + " " + selectedClass.displayName),
                selectedClass.color));
        nav.addRight(new ThemedButton(52, 24, Component.literal(">>"), theme,
                b -> reopen(currentIndex + 1)));
        content.addChild(nav);

        content.addChild(new ScaledLabel(0, 18,
                Component.literal("武器: " + getWeaponName(selectedClass) + "  操作難度: " + difficultyStars(selectedClass)),
                theme.valueText()));

        int[] stat = classStats(selectedClass);
        content.addChild(new ScaledLabel(0, 16,
                Component.literal("單體/防禦/機動/輔助/生存/群攻: "
                        + stat[0] + "/" + stat[1] + "/" + stat[2] + "/" + stat[3] + "/" + stat[4] + "/" + stat[5]),
                theme.labelText()));

        content.addChild(new ScaledLabel(0, 16,
                Component.literal(classFlavor(selectedClass)),
                theme.labelText()));

        content.addChild(new ScaledLabel(0, 16,
                Component.literal("快速選擇:"),
                theme.valueText()));

        for (int i = 0; i < CLASSES.length; i++) {
            final int rowIndex = i;
            PlayerClass cls = CLASSES[i];
            HBox row = new HBox(0, 0, 0, 30, 6, 2, 2);
            row.addFill(new ScaledLabel(0, 16,
                    Component.literal(cls.icon + " " + cls.displayName),
                    i == currentIndex ? cls.color : theme.labelText()));
            ThemedButton btn = row.addRight(new ThemedButton(84, 22,
                    Component.literal(i == currentIndex ? "目前" : "查看"),
                    theme,
                    b -> reopen(rowIndex)));
            btn.active = i != currentIndex;
            content.addChild(row);
        }

        HBox footer = new HBox(0, 0, 0, 34, 6, 2, 2);
        footer.addLeft(new ThemedButton(84, 24, Component.literal("返回"), theme,
                b -> minecraft.setScreen(new MainMenuScreen())));
        footer.addFill(new ScaledLabel(0, 16,
                Component.literal("職業一旦選定後無法更改"),
                0xFFFF8A8A));
        footer.addRight(new ThemedButton(112, 24, Component.literal("確定職業"), theme,
                b -> {
                    ModNetworking.sendToServer(new ClassSelectPacket(selectedClass));
                    onClose();
                }));
        content.addChild(footer);
    }

    private void reopen(int index) {
        int wrapped = (index % CLASSES.length + CLASSES.length) % CLASSES.length;
        if (minecraft != null) {
            minecraft.setScreen(new ClassSelectScreen(wrapped));
        }
    }

    private static int[] classStats(PlayerClass cls) {
        switch (cls) {
            case SWORDSMAN:
                return new int[]{7, 8, 5, 4, 7, 5};
            case MAGE:
                return new int[]{6, 3, 4, 7, 4, 9};
            case ARCHER:
                return new int[]{8, 4, 8, 3, 5, 5};
            case SUMMONER:
                return new int[]{5, 4, 4, 8, 5, 8};
            case WHITE_MAGE:
                return new int[]{4, 6, 4, 9, 8, 5};
            case ASSASSIN:
                return new int[]{9, 3, 9, 2, 4, 4};
            default:
                return new int[]{1, 1, 1, 1, 1, 1};
        }
    }

    private static String getWeaponName(PlayerClass cls) {
        switch (cls) {
            case SWORDSMAN:
                return "劍";
            case MAGE:
                return "法杖";
            case ARCHER:
                return "弓";
            case SUMMONER:
                return "法書";
            case WHITE_MAGE:
                return "聖杖";
            case ASSASSIN:
                return "短刃";
            default:
                return "-";
        }
    }

    private static String classFlavor(PlayerClass cls) {
        switch (cls) {
            case SWORDSMAN:
                return "擁有強大的防禦與生存能力，可持續壓制敵人並保護隊友。";
            case MAGE:
                return "擁有高額魔法爆發，擅長群體清場與控制節奏。";
            case ARCHER:
                return "以遠程精準輸出見長，具備優秀拉打能力。";
            case SUMMONER:
                return "透過召喚物創造壓力，適合多目標戰鬥。";
            case WHITE_MAGE:
                return "以治療與護佑見長，提供穩定續航。";
            case ASSASSIN:
                return "具備高機動與高爆發，擅長快速斬首。";
            default:
                return "尚未選擇職業。";
        }
    }

    private static String difficultyStars(PlayerClass cls) {
        switch (cls) {
            case SWORDSMAN:
                return "★★";
            case MAGE:
                return "★★★";
            case ARCHER:
                return "★★★";
            case SUMMONER:
                return "★★★★";
            case WHITE_MAGE:
                return "★★★";
            case ASSASSIN:
                return "★★★★★";
            default:
                return "★";
        }
    }

}

