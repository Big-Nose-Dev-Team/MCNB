package com.yichenxbohan.mcnb.combat.capability;

import com.yichenxbohan.mcnb.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class CombatDataProvider implements ICapabilityProvider {
    public static final ResourceLocation ID =
            new ResourceLocation("mcnb", "combat_data");

    private final CombatData data = new CombatData();
    private final LazyOptional<ICombatData> optional = LazyOptional.of(() -> data);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == ModCapabilities.COMBAT_DATA ? optional.cast() : LazyOptional.empty();
    }

    //存檔
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
    }
}
