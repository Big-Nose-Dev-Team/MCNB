package com.yichenxbohan.mcnb.client;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.client.gui.MainMenuScreen;
import com.yichenxbohan.mcnb.client.gui.StatsScreen;
import com.yichenxbohan.mcnb.network.ModNetworking;
import com.yichenxbohan.mcnb.network.SoulBowPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
    }
}
