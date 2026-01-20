package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.skill.SoulBowSkill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SoulBowPacket {

    public SoulBowPacket() {
    }

    public SoulBowPacket(FriendlyByteBuf buf) {
        // 目前不需要读取额外数据
    }

    public void toBytes(FriendlyByteBuf buf) {
        // 目前不需要写入额外数据
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 在服务端执行技能
                SoulBowSkill.activate(player);
            }
        });
        return true;
    }
}

