package com.yichenxbohan.mcnb;

import com.yichenxbohan.mcnb.combat.capability.ICombatData;
import com.yichenxbohan.mcnb.level.IEntityLevel;
import com.yichenxbohan.mcnb.level.IPlayerLevel;
import com.yichenxbohan.mcnb.playerclass.IPlayerClass;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Mcnb.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    public static final Capability<ICombatData> COMBAT_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IPlayerLevel> PLAYER_LEVEL =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IEntityLevel> ENTITY_LEVEL =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IPlayerClass> PLAYER_CLASS =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ICombatData.class);
        event.register(IPlayerLevel.class);
        event.register(IEntityLevel.class);
        event.register(IPlayerClass.class);
    }

    /** 取得玩家職業 Capability 的工具方法（避免跨套件泛型推斷問題） */
    public static IPlayerClass getPlayerClass(Player player) {
        return PLAYER_CLASS.<IPlayerClass>orEmpty(
                ModCapabilities.PLAYER_CLASS,
                player.getCapability(PLAYER_CLASS)
        ).orElse(null);
    }
}
