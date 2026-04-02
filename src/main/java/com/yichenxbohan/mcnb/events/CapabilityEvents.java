package com.yichenxbohan.mcnb.events;

import com.yichenxbohan.mcnb.combat.capability.CombatDataProvider;
import com.yichenxbohan.mcnb.playerclass.PlayerClassProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "mcnb")
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(
                    CombatDataProvider.ID,
                    new CombatDataProvider()
            );
        }
        // 玩家額外附加職業 Capability
        if (event.getObject() instanceof Player) {
            event.addCapability(
                    PlayerClassProvider.ID,
                    new PlayerClassProvider()
            );
        }
    }
}
