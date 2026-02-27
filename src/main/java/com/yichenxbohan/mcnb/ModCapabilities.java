package com.yichenxbohan.mcnb;

import com.yichenxbohan.mcnb.combat.capability.ICombatData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {

    public static final Capability<ICombatData> COMBAT_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});
}
