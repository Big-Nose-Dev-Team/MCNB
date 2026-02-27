package com.yichenxbohan.mcnb.client;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.client.damage.DamageNumberRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客戶端初始化事件
 * 用於註冊客戶端專用的事件監聽器
 */
@Mod.EventBusSubscriber(modid = Mcnb.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 註冊傷害數字渲染器到 Forge 事件總線
        MinecraftForge.EVENT_BUS.register(DamageNumberRenderer.class);
    }
}

