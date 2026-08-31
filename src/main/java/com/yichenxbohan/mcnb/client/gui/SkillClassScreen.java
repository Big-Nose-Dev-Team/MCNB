package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.client.KeyBindings;
import com.yichenxbohan.mcnb.client.gui.Libs.HBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ScrollVBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ScaledLabel;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedButton;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedPanelScreen;
import com.yichenxbohan.mcnb.client.gui.Libs.UITheme;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import com.yichenxbohan.mcnb.skill.api.SkillDefinition;
import com.yichenxbohan.mcnb.skill.api.SkillRegistry;
import com.yichenxbohan.mcnb.skill.capability.IPlayerSkillData;
import com.yichenxbohan.mcnb.skill.network.SkillPackets;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SkillClassScreen extends ThemedPanelScreen {
    private static final int ROW_H = 36;
    private static final int INFO_H = 16;
    private static final int ROW_GAP = 6;
    private static final KeyMapping[] SLOT_KEYS = {
            KeyBindings.SKILL_CAST_1,
            KeyBindings.SKILL_CAST_2,
            KeyBindings.SKILL_CAST_3,
            KeyBindings.SKILL_CAST_4,
            KeyBindings.SKILL_CAST_5,
            KeyBindings.SKILL_CAST_6
    };

    public SkillClassScreen() {
        super(Component.literal("技能"), 760, 420, UITheme.DEFAULT);
    }

    @Override
    protected void buildContent(ScrollVBox content) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            content.addChild(new ScaledLabel(0, 20, Component.literal("請先進入世界"), theme.labelText()));
            return;
        }

        PlayerClass playerClass = mc.player.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(cap -> cap.getPlayerClass())
                .orElse(PlayerClass.NONE);
        if (playerClass == PlayerClass.NONE) {
            content.addChild(new ScaledLabel(0, 20, Component.literal("尚未選擇職業，請先至職業面板選擇"), theme.labelText()));
            return;
        }

        int playerLevel = mc.player.getCapability(ModCapabilities.PLAYER_LEVEL)
                .map(cap -> cap.getLevel())
                .orElse(1);

        IPlayerSkillData skillData = mc.player.getCapability(ModCapabilities.PLAYER_SKILL).orElse(null);
        if (skillData == null) {
            content.addChild(new ScaledLabel(0, 20, Component.literal("技能資料尚未同步"), theme.labelText()));
            return;
        }

        int availablePoints = skillData.getAvailableSkillPoints(playerLevel);
        int spentPoints = skillData.getSpentSkillPoints();
        int contentWidth = Math.max(260, content.getContentWidth());
        int actionBtnW = Math.max(52, Math.min(72, contentWidth / 11));
        int slotBtnW = Math.max(116, Math.min(164, contentWidth / 4));

        HBox summaryRow = new HBox(0, 0, 0, ROW_H, ROW_GAP, 4, 4);
        summaryRow.addFill(new ScaledLabel(0, 20,
                Component.literal("職業: " + playerClass.displayName + "  等級: " + playerLevel
                        + "  已投入: " + spentPoints + "  可用技能點: " + availablePoints),
                theme.valueText()));
        ThemedButton resetBtn = summaryRow.addRight(new ThemedButton(72, 28, Component.literal("重置全部"), theme, b -> {
            // 【方案 A 修改】只發送請求，不在此處盲目刷新畫面
            SkillPackets.requestResetAll();
        }));
        resetBtn.active = skillData.getSpentSkillPoints() > 0;
        content.addChild(summaryRow);
        content.addChild(new ScaledLabel(0, INFO_H,
                Component.literal("提示: 槽位 1~6 對應快捷鍵施放，依職業技能清單順序連動"),
                theme.labelText()));

        List<SkillDefinition> skills = SkillRegistry.getSkills(playerClass);
        if (skills.isEmpty()) {
            content.addChild(new ScaledLabel(0, 20, Component.literal("此職業尚未配置技能"), theme.labelText()));
            return;
        }

        for (int i = 0; i < skills.size(); i++) {
            addSkillRow(content, skills.get(i), i, skillData, availablePoints, actionBtnW, slotBtnW);
        }
    }

    private void addSkillRow(ScrollVBox content, SkillDefinition skill, int index, IPlayerSkillData skillData,
                             int availablePoints, int actionBtnW, int slotBtnW) {
        int currentLevel = skillData.getSkillLevel(skill.getId());
        int maxLevel = skill.getMaxLevel();

        boolean canUpgrade = currentLevel < maxLevel && availablePoints > 0;
        boolean canUseSkill = currentLevel > 0;

        String statusText = currentLevel >= maxLevel
                ? "已滿級"
                : (availablePoints > 0 ? "可升級" : "點數不足");

        HBox row = new HBox(0, 0, 0, ROW_H, ROW_GAP, 4, 4);
        row.addFill(new ScaledLabel(220, 20,
                Component.literal((index + 1) + ". " + skill.getDisplayName()
                        + "  Lv." + currentLevel + "/" + maxLevel
                        + "  狀態:" + statusText),
                theme.valueText()));

        ThemedButton castBtn = row.addRight(new ThemedButton(actionBtnW, 28, Component.literal("施放"), theme,
                b -> SkillPackets.requestCast(skill.getId())));
        castBtn.active = canUseSkill;

        ThemedButton upgradeBtn = row.addRight(new ThemedButton(actionBtnW, 28, Component.literal("升級"), theme, b -> {
            // 【方案 A 修改】只發送請求，不在此處盲目刷新畫面
            SkillPackets.requestUpgrade(skill.getId());
        }));
        upgradeBtn.active = canUpgrade;

        String keyLabel = index < SLOT_KEYS.length
                ? SLOT_KEYS[index].getTranslatedKeyMessage().getString()
                : "-";
        row.addRight(new ThemedButton(slotBtnW, 28, Component.literal("槽位 " + (index + 1) + "  鍵:" + keyLabel), theme,
                b -> {
                    if (minecraft != null) {
                        minecraft.setScreen(new ControlsScreen(this, minecraft.options));
                    }
                }));

        content.addChild(row);
    }

    // 【方案 A 修改】將刷新方法改為 public，以便讓封包 Handler 跨類別呼叫
    public void refreshScreen() {
        if (minecraft != null) {
            minecraft.setScreen(new SkillClassScreen());
        }
    }
}