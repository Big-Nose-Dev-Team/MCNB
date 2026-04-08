package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.combat.StatsProvider;
import com.yichenxbohan.mcnb.events.PlayerLevelEvents;
import com.yichenxbohan.mcnb.level.PlayerAttributeType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客戶端 -> 服務端：調整玩家屬性點。
 */
public class AttributePointPacket {

    private final PlayerAttributeType type;
    private final int delta;
    private final boolean resetAll;

    public AttributePointPacket(PlayerAttributeType type, int delta) {
        this(type, delta, false);
    }

    public AttributePointPacket(boolean resetAll) {
        this(null, 0, resetAll);
    }

    private AttributePointPacket(PlayerAttributeType type, int delta, boolean resetAll) {
        this.type = type;
        this.delta = delta;
        this.resetAll = resetAll;
    }

    public AttributePointPacket(FriendlyByteBuf buf) {
        this.resetAll = buf.readBoolean();
        this.type = resetAll ? null : PlayerAttributeType.fromOrdinal(buf.readVarInt());
        this.delta = resetAll ? 0 : buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(resetAll);
        if (!resetAll) {
            buf.writeVarInt(type.ordinal());
            buf.writeVarInt(delta);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(cap -> {
                boolean changed;
                if (resetAll) {
                    changed = cap.resetAttributePoints();
                } else {
                    changed = cap.adjustAttributePoints(type, delta);
                }

                if (changed) {
                    StatsProvider.apply(player);
                    PlayerLevelEvents.syncToClient(player);
                    PlayerLevelEvents.syncStatsToClient(player);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}


