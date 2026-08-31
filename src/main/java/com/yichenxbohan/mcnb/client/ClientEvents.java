package com.yichenxbohan.mcnb.client;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.client.renderer.CustomBillboardRenderer;
import com.yichenxbohan.mcnb.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Mcnb.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.SOUL_BOW_KEY);
        event.register(KeyBindings.STATS_PANEL_KEY);
        event.register(KeyBindings.MAIN_MENU_KEY);
        event.register(KeyBindings.SKILL_CAST_1);
        event.register(KeyBindings.SKILL_CAST_2);
        event.register(KeyBindings.SKILL_CAST_3);
        event.register(KeyBindings.SKILL_CAST_4);
        event.register(KeyBindings.SKILL_CAST_5);
        event.register(KeyBindings.SKILL_CAST_6);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 綁定你剛剛修改好的直接讀取 Asset 的自訂廣告看板渲染器
        event.registerEntityRenderer(ModEntities.DEATH_STARE.get(), CustomBillboardRenderer::new);
    }
}
