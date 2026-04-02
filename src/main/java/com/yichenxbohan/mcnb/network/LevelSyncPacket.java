package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 伺服器 -> 客戶端：同步玩家等級與經驗
 */
public class LevelSyncPacket {

    private final int level;
    private final long exp;

    public LevelSyncPacket(int level, long exp) {
        this.level = level;
        this.exp = exp;
    }

    public LevelSyncPacket(FriendlyByteBuf buf) {
        this.level = buf.readVarInt();
        this.exp = buf.readVarLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(level);
        buf.writeVarLong(exp);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient())
        );
        ctx.get().setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private void handleClient() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(cap -> {
            cap.setLevel(level);
            cap.setExp(exp);
            cap.clearDirty();
        });
    }
}

