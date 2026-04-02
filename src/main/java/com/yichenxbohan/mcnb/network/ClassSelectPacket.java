package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.playerclass.IPlayerClass;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客戶端 -> 服務端：選擇職業
 */
public class ClassSelectPacket {

    private final PlayerClass playerClass;

    public ClassSelectPacket(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public ClassSelectPacket(FriendlyByteBuf buf) {
        this.playerClass = buf.readEnum(PlayerClass.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(playerClass);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            IPlayerClass cap = ModCapabilities.getPlayerClass(player);
            if (cap != null) {
                cap.setPlayerClass(playerClass);
                cap.clearDirty();
            }

            // 同步回客戶端
            ModNetworking.sendToPlayer(new ClassSyncPacket(playerClass), player);
        });
        ctx.get().setPacketHandled(true);
    }
}
