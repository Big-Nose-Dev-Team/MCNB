package com.yichenxbohan.mcnb.client;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.network.ModNetworking;
import com.yichenxbohan.mcnb.network.SoulBowPacket;
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
    }
}

