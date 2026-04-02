package com.yichenxbohan.mcnb.client;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.client.damage.DamageNumberRenderer;
import com.yichenxbohan.mcnb.client.level.PlayerLevelHudRenderer;
import com.yichenxbohan.mcnb.client.particle.EndPortalParticleHandler;
import com.yichenxbohan.mcnb.particle.ModParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import team.lodestar.lodestone.systems.particle.type.LodestoneParticleType;

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
        MinecraftForge.EVENT_BUS.register(PlayerLevelHudRenderer.class);
        // 註冊終界傳送門核心粒子驅動器
        MinecraftForge.EVENT_BUS.register(EndPortalParticleHandler.class);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.END_SLASH.get(), LodestoneParticleType.Factory::new);
    }
}
