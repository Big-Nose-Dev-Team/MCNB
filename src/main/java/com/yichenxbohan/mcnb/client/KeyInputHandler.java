package com.yichenxbohan.mcnb.client;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.client.gui.MainMenuScreen;
import com.yichenxbohan.mcnb.client.gui.StatsScreen;
import com.yichenxbohan.mcnb.network.ModNetworking;
import com.yichenxbohan.mcnb.network.SoulBowPacket;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import com.yichenxbohan.mcnb.skill.api.SkillDefinition;
import com.yichenxbohan.mcnb.skill.api.SkillRegistry;
import com.yichenxbohan.mcnb.skill.network.SkillPackets;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Mcnb.MODID, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.SOUL_BOW_KEY.consumeClick()) {
            // 发送数据包到服务端
            ModNetworking.sendToServer(new SoulBowPacket());
        }
        if (KeyBindings.STATS_PANEL_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen(new StatsScreen());
            }
        }
        if (KeyBindings.MAIN_MENU_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen(new MainMenuScreen());
            }
        }

        if (KeyBindings.SKILL_CAST_1.consumeClick()) triggerSkillBySlot(0);
        if (KeyBindings.SKILL_CAST_2.consumeClick()) triggerSkillBySlot(1);
        if (KeyBindings.SKILL_CAST_3.consumeClick()) triggerSkillBySlot(2);
        if (KeyBindings.SKILL_CAST_4.consumeClick()) triggerSkillBySlot(3);
        if (KeyBindings.SKILL_CAST_5.consumeClick()) triggerSkillBySlot(4);
        if (KeyBindings.SKILL_CAST_6.consumeClick()) triggerSkillBySlot(5);
    }

    private static void triggerSkillBySlot(int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        PlayerClass clazz = mc.player.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(c -> c.getPlayerClass())
                .orElse(PlayerClass.NONE);
        if (clazz == PlayerClass.NONE) {
            return;
        }

        List<SkillDefinition> skills = SkillRegistry.getSkills(clazz);
        if (slot < 0 || slot >= skills.size()) {
            return;
        }

        SkillPackets.requestCast(skills.get(slot).getId());
    }
}
