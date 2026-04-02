package com.yichenxbohan.mcnb.playerclass;

import com.yichenxbohan.mcnb.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerClassProvider implements ICapabilitySerializable<CompoundTag> {

    public static final ResourceLocation ID =
            new ResourceLocation("mcnb", "player_class");

    private final PlayerClassData data = new PlayerClassData();
    private final LazyOptional<IPlayerClass> optional = LazyOptional.of(() -> data);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == ModCapabilities.PLAYER_CLASS ? optional.cast() : LazyOptional.empty();
    }

    @Override public CompoundTag serializeNBT()               { return data.serializeNBT(); }
    @Override public void        deserializeNBT(CompoundTag t) { data.deserializeNBT(t); }

    public void invalidate() { optional.invalidate(); }
}
