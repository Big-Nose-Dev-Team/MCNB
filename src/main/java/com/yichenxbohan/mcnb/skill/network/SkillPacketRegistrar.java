package com.yichenxbohan.mcnb.skill.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.IntSupplier;

/**
 * 技能模組封包註冊入口。
 */
public final class SkillPacketRegistrar {

    private SkillPacketRegistrar() {
    }

    public static void register(SimpleChannel channel, IntSupplier idSupplier) {
        channel.messageBuilder(SkillActionPacket.class, idSupplier.getAsInt(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SkillActionPacket::new)
                .encoder(SkillActionPacket::toBytes)
                .consumerMainThread(SkillActionPacket::handle)
                .add();

        channel.messageBuilder(SkillSyncPacket.class, idSupplier.getAsInt(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SkillSyncPacket::new)
                .encoder(SkillSyncPacket::toBytes)
                .consumerMainThread(SkillSyncPacket::handle)
                .add();
    }
}

