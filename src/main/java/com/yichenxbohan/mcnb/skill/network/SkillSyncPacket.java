package com.yichenxbohan.mcnb.skill.network;

import com.yichenxbohan.mcnb.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * S2C 技能資料同步封包。
 */
public class SkillSyncPacket {

    private final Map<String, Integer> levels;
    private final Map<String, String> branches;

    public SkillSyncPacket(Map<String, Integer> levels, Map<String, String> branches) {
        this.levels = new HashMap<>(levels);
        this.branches = new HashMap<>(branches);
    }

    public SkillSyncPacket(FriendlyByteBuf buf) {
        int levelSize = buf.readVarInt();
        this.levels = new HashMap<>();
        for (int i = 0; i < levelSize; i++) {
            String key = buf.readUtf(128);
            int value = buf.readVarInt();
            levels.put(key, value);
        }

        int branchSize = buf.readVarInt();
        this.branches = new HashMap<>();
        for (int i = 0; i < branchSize; i++) {
            String key = buf.readUtf(128);
            String value = buf.readUtf(128);
            branches.put(key, value);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(levels.size());
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }

        buf.writeVarInt(branches.size());
        for (Map.Entry<String, String> entry : branches.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleClient)
        );
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.getCapability(ModCapabilities.PLAYER_SKILL).ifPresent(cap -> {
            cap.overwriteFrom(levels, branches);
            cap.clearDirty();
        });
    }
}

