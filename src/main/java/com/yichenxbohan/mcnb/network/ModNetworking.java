package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.skill.network.SkillPacketRegistrar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Mcnb.MODID, "main"),
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

        // 服務端 -> 客戶端：等級/經驗同步
        INSTANCE.messageBuilder(LevelSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(LevelSyncPacket::new)
                .encoder(LevelSyncPacket::toBytes)
                .consumerMainThread(LevelSyncPacket::handle)
                .add();

        // 服務端 -> 客戶端：升級通知
        INSTANCE.messageBuilder(LevelUpPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(LevelUpPacket::new)
                .encoder(LevelUpPacket::toBytes)
                .consumerMainThread(LevelUpPacket::handle)
                .add();

        // 服務端 -> 客戶端：戰鬥屬性同步
        INSTANCE.messageBuilder(StatsSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(StatsSyncPacket::new)
                .encoder(StatsSyncPacket::toBytes)
                .consumerMainThread(StatsSyncPacket::handle)
                .add();

        // 客戶端 -> 服務端：調整屬性點
        INSTANCE.messageBuilder(AttributePointPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(AttributePointPacket::new)
                .encoder(AttributePointPacket::toBytes)
                .consumerMainThread(AttributePointPacket::handle)
                .add();

        // 客戶端 -> 服務端：選擇職業
        INSTANCE.messageBuilder(ClassSelectPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClassSelectPacket::new)
                .encoder(ClassSelectPacket::toBytes)
                .consumerMainThread(ClassSelectPacket::handle)
                .add();

        // 服務端 -> 客戶端：同步職業
        INSTANCE.messageBuilder(ClassSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClassSyncPacket::new)
                .encoder(ClassSyncPacket::toBytes)
                .consumerMainThread(ClassSyncPacket::handle)
                .add();

        // 技能系統封包（API 自動註冊）
        SkillPacketRegistrar.register(INSTANCE, ModNetworking::id);
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
