package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.client.level.LevelUpNotifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 伺服器 -> 客戶端：升級通知
 */
public class LevelUpPacket {

    private final int newLevel;

    public LevelUpPacket(int newLevel) {
        this.newLevel = newLevel;
    }

    public LevelUpPacket(FriendlyByteBuf buf) {
        this.newLevel = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(newLevel);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient())
        );
        ctx.get().setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private void handleClient() {
        LevelUpNotifier.showLevelUp(newLevel);
    }
}

