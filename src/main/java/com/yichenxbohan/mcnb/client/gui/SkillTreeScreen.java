package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.client.gui.Libs.HBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ScaledLabel;
import com.yichenxbohan.mcnb.client.gui.Libs.ScrollVBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedButton;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedPanelScreen;
import com.yichenxbohan.mcnb.client.gui.Libs.UITheme;
import com.yichenxbohan.mcnb.skill.api.SkillDefinition;
import com.yichenxbohan.mcnb.skill.api.SkillRegistry;
import com.yichenxbohan.mcnb.skill.network.SkillPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkillTreeScreen extends ThemedPanelScreen {

    private final String skillId;

    public SkillTreeScreen(String skillId) {
        super(Component.literal("技能詳情"), 760, 420, UITheme.DEFAULT);
        this.skillId = skillId;
    }

    @Override
    protected void buildContent(ScrollVBox content) {
        SkillDefinition skill = SkillRegistry.getById(skillId);
        if (skill == null) {
            content.addChild(new ScaledLabel(0, 20, Component.literal("技能不存在"), 0xFFFF8888));
            return;
        }

        int level = getSkillLevel(skill.getId());
        int cooldown = getRemainingCooldown(skill.getId());
        String cooldownText = cooldown > 0 ? String.format("%.1f 秒", cooldown / 20.0f) : "可施放";

        content.addChild(new ScaledLabel(0, 20,
                Component.literal(skill.getDisplayName() + "  Lv." + level + "/" + skill.getMaxLevel()),
                theme.titleText()));
        content.addChild(new ScaledLabel(0, 16,
                Component.literal(skill.getDescription()),
                theme.labelText()));
        content.addChild(new ScaledLabel(0, 16,
                Component.literal("冷卻時間: " + (skill.getCooldownTicks() / 20.0f) + " 秒  目前狀態: " + cooldownText),
                theme.valueText()));

        HBox actionRow = new HBox(0, 0, 0, 34, 6, 2, 2);
        actionRow.addLeft(new ThemedButton(84, 24, Component.literal("返回"), theme,
                b -> minecraft.setScreen(new SkillClassScreen())));
        actionRow.addFill(new ScaledLabel(0, 16,
                Component.literal("技能等級: " + level + " / " + skill.getMaxLevel()),
                theme.labelText()));
        actionRow.addRight(new ThemedButton(96, 24, Component.literal("施放技能"), theme,
                b -> SkillPackets.requestCast(skill.getId())));
        content.addChild(actionRow);
    }

    private int getSkillLevel(String id) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        return player.getCapability(ModCapabilities.PLAYER_SKILL).map(cap -> cap.getSkillLevel(id)).orElse(0);
    }

    private int getRemainingCooldown(String id) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        long gameTick = player.level() == null ? 0L : player.level().getGameTime();
        return player.getCapability(ModCapabilities.PLAYER_SKILL)
                .map(cap -> cap.getRemainingCooldownTicks(id, gameTick))
                .orElse(0);
    }
}

