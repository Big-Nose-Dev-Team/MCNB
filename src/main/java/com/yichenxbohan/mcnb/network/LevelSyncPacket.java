package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 伺服器 -> 客戶端：同步玩家等級與經驗
 */
public class LevelSyncPacket {

    private final int level;
    private final long exp;
    private final int strength;
    private final int constitution;
    private final int potential;
    private final int intelligence;
    private final int agility;

    public LevelSyncPacket(int level, long exp, int strength, int constitution, int potential, int intelligence, int agility) {
        this.level = level;
        this.exp = exp;
        this.strength = strength;
        this.constitution = constitution;
        this.potential = potential;
        this.intelligence = intelligence;
        this.agility = agility;
    }

    public LevelSyncPacket(FriendlyByteBuf buf) {
        this.level = buf.readVarInt();
        this.exp = buf.readVarLong();
        this.strength = buf.readVarInt();
        this.constitution = buf.readVarInt();
        this.potential = buf.readVarInt();
        this.intelligence = buf.readVarInt();
        this.agility = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(level);
        buf.writeVarLong(exp);
        buf.writeVarInt(strength);
        buf.writeVarInt(constitution);
        buf.writeVarInt(potential);
        buf.writeVarInt(intelligence);
        buf.writeVarInt(agility);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(this::handleClient);
        ctx.get().setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private void handleClient() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(cap -> {
            cap.setLevel(level);
            cap.setExp(exp);
            cap.setAttributePoints(strength, constitution, potential, intelligence, agility);
            cap.clearDirty();
        });
    }
}

