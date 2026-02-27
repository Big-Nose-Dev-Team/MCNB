package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.Mcnb;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mcnb.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // 客戶端 -> 服務端
        INSTANCE.messageBuilder(SoulBowPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SoulBowPacket::new)
                .encoder(SoulBowPacket::toBytes)
                .consumerMainThread(SoulBowPacket::handle)
                .add();

        // 服務端 -> 客戶端：傷害數字
        INSTANCE.messageBuilder(DamageNumberPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DamageNumberPacket::new)
                .encoder(DamageNumberPacket::toBytes)
                .consumerMainThread(DamageNumberPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    /**
     * 發送訊息給所有追蹤某位置的玩家
     */
    public static <MSG> void sendToAllTracking(MSG message, net.minecraft.world.entity.Entity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    /**
     * 發送訊息給指定範圍內的所有玩家
     */
    public static <MSG> void sendToNearby(MSG message, net.minecraft.server.level.ServerLevel level,
                                           double x, double y, double z, double radius) {
        INSTANCE.send(PacketDistributor.NEAR.with(() ->
            new PacketDistributor.TargetPoint(x, y, z, radius, level.dimension())), message);
    }
}
