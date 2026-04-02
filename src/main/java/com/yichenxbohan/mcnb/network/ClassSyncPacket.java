package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.playerclass.IPlayerClass;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服務端 -> 客戶端：同步玩家職業
 */
public class ClassSyncPacket {

    private final PlayerClass playerClass;

    public ClassSyncPacket(PlayerClass playerClass) { this.playerClass = playerClass; }

    public ClassSyncPacket(FriendlyByteBuf buf) {
        this.playerClass = buf.readEnum(PlayerClass.class);
    }

    public void toBytes(FriendlyByteBuf buf) { buf.writeEnum(playerClass); }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleClient)
        );
        ctx.get().setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        IPlayerClass cap = ModCapabilities.getPlayerClass(player);
        if (cap != null) {
            cap.setPlayerClass(playerClass);
            cap.clearDirty();
        }
    }
}
