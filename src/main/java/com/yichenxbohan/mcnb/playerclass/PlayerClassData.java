package com.yichenxbohan.mcnb.playerclass;

import net.minecraft.nbt.CompoundTag;

public class PlayerClassData implements IPlayerClass {

    private PlayerClass playerClass = PlayerClass.NONE;
    private boolean dirty = false;

    @Override
    public PlayerClass getPlayerClass() { return playerClass; }

    @Override
    public void setPlayerClass(PlayerClass cls) {
        this.playerClass = cls == null ? PlayerClass.NONE : cls;
        markDirty();
    }

    @Override
    public boolean hasClass() { return playerClass != PlayerClass.NONE; }

    @Override public void markDirty()  { dirty = true; }
    @Override public boolean isDirty() { return dirty; }
    @Override public void clearDirty() { dirty = false; }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("playerClass", playerClass.name());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("playerClass")) {
            try {
                playerClass = PlayerClass.valueOf(tag.getString("playerClass"));
            } catch (IllegalArgumentException e) {
                playerClass = PlayerClass.NONE;
            }
        }
    }
}

